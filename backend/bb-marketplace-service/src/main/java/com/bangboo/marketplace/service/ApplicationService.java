package com.bangboo.marketplace.service;

import com.bangboo.common.exception.BusinessException;
import com.bangboo.common.exception.ForbiddenException;
import com.bangboo.common.exception.ResourceNotFoundException;
import com.bangboo.common.response.PageResult;
import com.bangboo.common.security.CurrentUser;
import com.bangboo.marketplace.client.InternalApiClient;
import com.bangboo.marketplace.dto.CreateApplicationRequest;
import com.bangboo.marketplace.dto.TaskApplicationVO;
import com.bangboo.marketplace.dto.TaskContractVO;
import com.bangboo.marketplace.dto.UpdateApplicationRequest;
import com.bangboo.marketplace.dto.internal.CreateMessageRequest;
import com.bangboo.marketplace.dto.internal.UserBriefVO;
import com.bangboo.marketplace.entity.Task;
import com.bangboo.marketplace.entity.TaskApplication;
import com.bangboo.marketplace.entity.TaskContract;
import com.bangboo.marketplace.enums.ApplicationStatus;
import com.bangboo.marketplace.enums.ContractStatus;
import com.bangboo.marketplace.enums.TaskStatus;
import com.bangboo.marketplace.repository.TaskApplicationRepository;
import com.bangboo.marketplace.repository.TaskContractRepository;
import com.bangboo.marketplace.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {
    private final TaskRepository taskRepository;
    private final TaskApplicationRepository applicationRepository;
    private final TaskContractRepository contractRepository;
    private final MarketplaceAuthService authService;
    private final InternalApiClient internalApiClient;
    private final IdGenerator idGenerator;

    public ApplicationService(
            TaskRepository taskRepository,
            TaskApplicationRepository applicationRepository,
            TaskContractRepository contractRepository,
            MarketplaceAuthService authService,
            InternalApiClient internalApiClient,
            IdGenerator idGenerator
    ) {
        this.taskRepository = taskRepository;
        this.applicationRepository = applicationRepository;
        this.contractRepository = contractRepository;
        this.authService = authService;
        this.internalApiClient = internalApiClient;
        this.idGenerator = idGenerator;
    }

    /** 提交接单申请：登录 + 校园认证；任务须 PUBLISHED；不能申请自己的任务；同任务同用户不可重复 APPLIED。 */
    @Transactional
    public TaskApplicationVO apply(Long taskId, CreateApplicationRequest request) {
        CurrentUser user = authService.requireActiveVerifiedUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));

        if (user.uid().equals(task.getPublisherId())) {
            throw new ForbiddenException("不能申请自己发布的任务");
        }
        if (task.getStatus() != TaskStatus.PUBLISHED) {
            throw new BusinessException(409, "该任务当前不可申请");
        }
        applicationRepository.findByTaskIdAndHunterIdAndStatus(taskId, user.uid(), ApplicationStatus.APPLIED)
                .ifPresent(existing -> {
                    throw new BusinessException(409, "你已申请过该任务");
                });

        TaskApplication application = new TaskApplication();
        application.setId(idGenerator.nextApplicationId());
        application.setTaskId(taskId);
        application.setTaskTitle(task.getTitle());
        application.setHunterId(user.uid());
        application.setPublisherId(task.getPublisherId());
        application.setApplyMessage(request.applyMessage());
        application.setExpectedFinishTime(request.expectedFinishTime());
        application.setStatus(ApplicationStatus.APPLIED);
        applyHunterSnapshot(application, user.uid());
        TaskApplication saved = applicationRepository.save(application);

        task.setApplicationCount(applicationRepository.countByTaskIdIntValue(taskId));
        taskRepository.save(task);

        // 旁路：通知委托人（失败不回滚）
        internalApiClient.createMessage(new CreateMessageRequest(
                task.getPublisherId(),
                "APPLICATION",
                "新的接单申请",
                "你的任务《" + task.getTitle() + "》收到一条新申请",
                task.getId()
        ));
        return MarketplaceMapper.toApplicationVO(saved);
    }

    /** 查看任务申请列表：仅委托人或管理员。 */
    @Transactional(readOnly = true)
    public List<TaskApplicationVO> listByTask(Long taskId) {
        CurrentUser user = authService.requireUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));
        if (!authService.isAdmin(user) && !user.uid().equals(task.getPublisherId())) {
            throw new ForbiddenException("只有委托人可查看申请列表");
        }
        return applicationRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream()
                .map(MarketplaceMapper::toApplicationVO)
                .toList();
    }

    /** 我的申请。 */
    @Transactional(readOnly = true)
    public PageResult<TaskApplicationVO> my(int page, int size, String status) {
        CurrentUser user = authService.requireUser();
        Pageable pageable = PageRequest.of(page, size <= 0 ? 10 : size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TaskApplication> result;
        if (status != null && !status.isBlank()) {
            result = applicationRepository.findByHunterIdAndStatus(user.uid(), ApplicationStatus.valueOf(status), pageable);
        } else {
            result = applicationRepository.findByHunterId(user.uid(), pageable);
        }
        return PageMapper.toPageResult(result, MarketplaceMapper::toApplicationVO);
    }

    /** 修改申请：仅本人；仅 APPLIED。 */
    @Transactional
    public TaskApplicationVO update(Long id, UpdateApplicationRequest request) {
        CurrentUser user = authService.requireUser();
        TaskApplication application = requireOwnApplication(id, user);
        if (application.getStatus() != ApplicationStatus.APPLIED) {
            throw new BusinessException(409, "当前申请状态不可修改");
        }
        if (request.applyMessage() != null) {
            application.setApplyMessage(request.applyMessage());
        }
        if (request.expectedFinishTime() != null) {
            application.setExpectedFinishTime(request.expectedFinishTime());
        }
        return MarketplaceMapper.toApplicationVO(applicationRepository.save(application));
    }

    /** 撤销申请：仅本人；仅 APPLIED。 */
    @Transactional
    public TaskApplicationVO cancel(Long id) {
        CurrentUser user = authService.requireUser();
        TaskApplication application = requireOwnApplication(id, user);
        if (application.getStatus() != ApplicationStatus.APPLIED) {
            throw new BusinessException(409, "当前申请不可撤销");
        }
        application.setStatus(ApplicationStatus.CANCELLED);
        application.setCancelledAt(Instant.now());
        TaskApplication saved = applicationRepository.save(application);
        adjustTaskApplicationCount(application.getTaskId());
        return MarketplaceMapper.toApplicationVO(saved);
    }

    /** 拒绝申请：仅委托人；仅 APPLIED。 */
    @Transactional
    public TaskApplicationVO reject(Long id) {
        CurrentUser user = authService.requireUser();
        TaskApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("申请不存在"));
        if (!user.uid().equals(application.getPublisherId()) && !authService.isAdmin(user)) {
            throw new ForbiddenException("只有委托人可以处理该申请");
        }
        if (application.getStatus() != ApplicationStatus.APPLIED) {
            throw new BusinessException(409, "当前申请状态不可拒绝");
        }
        application.setStatus(ApplicationStatus.REJECTED);
        TaskApplication saved = applicationRepository.save(application);

        internalApiClient.createMessage(new CreateMessageRequest(
                application.getHunterId(),
                "APPLICATION",
                "申请未通过",
                "你对《" + application.getTaskTitle() + "》的申请未被选中",
                application.getTaskId()
        ));
        return MarketplaceMapper.toApplicationVO(saved);
    }

    /**
     * 接受申请（本地事务）：
     * 1) 当前申请 -> ACCEPTED
     * 2) 同任务其他 APPLIED -> REJECTED
     * 3) 任务 -> IN_PROGRESS，写 assignedHunterId
     * 4) 创建 IN_PROGRESS 契约（拷贝快照与完成标准/证据要求）
     * 返回新契约 VO。
     */
    @Transactional
    public TaskContractVO accept(Long id) {
        CurrentUser user = authService.requireUser();
        TaskApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("申请不存在"));
        Task task = taskRepository.findById(application.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));

        if (!user.uid().equals(task.getPublisherId()) && !authService.isAdmin(user)) {
            throw new ForbiddenException("只有委托人可以选择猎人");
        }
        if (application.getStatus() != ApplicationStatus.APPLIED) {
            throw new BusinessException(409, "该申请当前不可接受");
        }
        if (task.getStatus() != TaskStatus.PUBLISHED) {
            throw new BusinessException(409, "该任务当前不可选择猎人");
        }
        if (contractRepository.findByTaskId(task.getId()).isPresent()) {
            throw new BusinessException(409, "该任务已存在有效契约");
        }

        Instant now = Instant.now();
        application.setStatus(ApplicationStatus.ACCEPTED);
        application.setAcceptedAt(now);
        applicationRepository.save(application);

        // 其他 APPLIED -> REJECTED
        List<TaskApplication> others = applicationRepository.findByTaskIdAndStatus(task.getId(), ApplicationStatus.APPLIED);
        for (TaskApplication other : others) {
            if (!other.getId().equals(application.getId())) {
                other.setStatus(ApplicationStatus.REJECTED);
                applicationRepository.save(other);
            }
        }

        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setAssignedHunterId(application.getHunterId());
        taskRepository.save(task);

        TaskContract contract = new TaskContract();
        Long contractId = idGenerator.nextContractId();
        contract.setId(contractId);
        contract.setContractNo("CONTRACT-" + contractId);
        contract.setTaskId(task.getId());
        contract.setTaskTitle(task.getTitle());
        contract.setApplicationId(application.getId());
        contract.setPublisherId(task.getPublisherId());
        contract.setPublisherName(task.getPublisherName());
        contract.setPublisherAvatar(task.getPublisherAvatar());
        contract.setHunterId(application.getHunterId());
        contract.setHunterName(application.getHunterName());
        contract.setHunterAvatar(application.getHunterAvatar());
        contract.setBountyAmount(task.getBountyAmount());
        contract.setBountyType(task.getBountyType());
        contract.setStatus(ContractStatus.IN_PROGRESS);
        contract.setCompletionStandard(task.getCompletionStandard());
        contract.setEvidenceRequirement(task.getEvidenceRequirement());
        contract.setReviewedByPublisher(false);
        contract.setReviewedByHunter(false);
        contract.setAcceptedAt(now);
        TaskContract savedContract = contractRepository.save(contract);

        // 旁路：通知猎人（失败不回滚）
        internalApiClient.createMessage(new CreateMessageRequest(
                application.getHunterId(),
                "CONTRACT",
                "你已被选中",
                "你已被选为《" + task.getTitle() + "》的猎人，契约已创建",
                savedContract.getId()
        ));
        return MarketplaceMapper.toContractVO(savedContract);
    }

    // ---------- helpers ----------

    private TaskApplication requireOwnApplication(Long id, CurrentUser user) {
        TaskApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("申请不存在"));
        if (!user.uid().equals(application.getHunterId())) {
            throw new ForbiddenException("只能操作自己的申请");
        }
        return application;
    }

    private void adjustTaskApplicationCount(Long taskId) {
        taskRepository.findById(taskId).ifPresent(task -> {
            task.setApplicationCount(applicationRepository.countByTaskIdIntValue(taskId));
            taskRepository.save(task);
        });
    }

    private void applyHunterSnapshot(TaskApplication application, Long hunterId) {
        Optional<UserBriefVO> brief = internalApiClient.userBrief(hunterId);
        if (brief.isPresent()) {
            UserBriefVO u = brief.get();
            application.setHunterName(u.nickname() != null ? u.nickname() : u.username());
            application.setHunterAvatar(u.avatarUrl());
            application.setHunterLevel(u.hunterLevel());
            application.setHunterTitle(u.hunterTitle());
            application.setHunterReputation(u.reputation());
        } else {
            application.setHunterName("用户" + hunterId);
            application.setHunterAvatar("bangboo:kacha");
        }
    }
}
