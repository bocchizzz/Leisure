package com.bangboo.marketplace.service;

import com.bangboo.common.exception.BusinessException;
import com.bangboo.common.exception.ForbiddenException;
import com.bangboo.common.exception.ResourceNotFoundException;
import com.bangboo.common.response.PageResult;
import com.bangboo.common.security.CurrentUser;
import com.bangboo.marketplace.client.InternalApiClient;
import com.bangboo.marketplace.dto.CancelContractRequest;
import com.bangboo.marketplace.dto.CreateEvidenceRequest;
import com.bangboo.marketplace.dto.TaskContractVO;
import com.bangboo.marketplace.dto.TaskEvidenceVO;
import com.bangboo.marketplace.dto.internal.ContractBriefVO;
import com.bangboo.marketplace.dto.internal.ContractRuleResultRequest;
import com.bangboo.marketplace.dto.internal.CreateMessageRequest;
import com.bangboo.marketplace.dto.internal.MarkDisputedRequest;
import com.bangboo.marketplace.dto.internal.MarketplaceStatsVO;
import com.bangboo.marketplace.dto.internal.ReviewFlagRequest;
import com.bangboo.marketplace.dto.internal.ReviewPermissionVO;
import com.bangboo.marketplace.entity.Task;
import com.bangboo.marketplace.entity.TaskContract;
import com.bangboo.marketplace.entity.TaskEvidence;
import com.bangboo.marketplace.enums.ContractStatus;
import com.bangboo.marketplace.enums.EvidenceType;
import com.bangboo.marketplace.enums.TaskStatus;
import com.bangboo.marketplace.repository.TaskContractRepository;
import com.bangboo.marketplace.repository.TaskEvidenceRepository;
import com.bangboo.marketplace.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ContractService {
    private final TaskContractRepository contractRepository;
    private final TaskRepository taskRepository;
    private final TaskEvidenceRepository evidenceRepository;
    private final MarketplaceAuthService authService;
    private final InternalApiClient internalApiClient;
    private final IdGenerator idGenerator;

    public ContractService(
            TaskContractRepository contractRepository,
            TaskRepository taskRepository,
            TaskEvidenceRepository evidenceRepository,
            MarketplaceAuthService authService,
            InternalApiClient internalApiClient,
            IdGenerator idGenerator
    ) {
        this.contractRepository = contractRepository;
        this.taskRepository = taskRepository;
        this.evidenceRepository = evidenceRepository;
        this.authService = authService;
        this.internalApiClient = internalApiClient;
        this.idGenerator = idGenerator;
    }

    // ---------- 对外接口 ----------

    /** 契约详情：仅双方或管理员。 */
    @Transactional(readOnly = true)
    public TaskContractVO getById(Long id) {
        CurrentUser user = authService.requireUser();
        TaskContract contract = requireContract(id);
        requirePartyOrAdmin(user, contract, "非任务双方不能查看契约");
        return MarketplaceMapper.toContractVO(contract);
    }

    @Transactional(readOnly = true)
    public PageResult<TaskContractVO> myPublished(int page, int size, String status) {
        CurrentUser user = authService.requireUser();
        Pageable pageable = pageable(page, size);
        Page<TaskContract> result = status != null && !status.isBlank()
                ? contractRepository.findByPublisherIdAndStatus(user.uid(), ContractStatus.valueOf(status), pageable)
                : contractRepository.findByPublisherId(user.uid(), pageable);
        return PageMapper.toPageResult(result, MarketplaceMapper::toContractVO);
    }

    @Transactional(readOnly = true)
    public PageResult<TaskContractVO> myAccepted(int page, int size, String status) {
        CurrentUser user = authService.requireUser();
        Pageable pageable = pageable(page, size);
        Page<TaskContract> result = status != null && !status.isBlank()
                ? contractRepository.findByHunterIdAndStatus(user.uid(), ContractStatus.valueOf(status), pageable)
                : contractRepository.findByHunterId(user.uid(), pageable);
        return PageMapper.toPageResult(result, MarketplaceMapper::toContractVO);
    }

    /** 提交履约证据：仅猎人；仅 IN_PROGRESS；fileUrl 与 content 至少一个非空。 */
    @Transactional
    public TaskEvidenceVO submitEvidence(Long id, CreateEvidenceRequest request) {
        CurrentUser user = authService.requireUser();
        TaskContract contract = requireContract(id);
        if (!user.uid().equals(contract.getHunterId())) {
            throw new ForbiddenException("只有猎人可提交履约证据");
        }
        if (contract.getStatus() != ContractStatus.IN_PROGRESS) {
            throw new BusinessException(409, "当前契约状态不可提交证据");
        }
        boolean hasFile = request.fileUrl() != null && !request.fileUrl().isBlank();
        boolean hasContent = request.content() != null && !request.content().isBlank();
        if (!hasFile && !hasContent) {
            throw new BusinessException(400, "请提供证据文件或说明");
        }

        TaskEvidence evidence = new TaskEvidence();
        evidence.setId(idGenerator.nextEvidenceId());
        evidence.setContractId(contract.getId());
        evidence.setTaskId(contract.getTaskId());
        evidence.setSubmitterId(user.uid());
        evidence.setSubmitterName(contract.getHunterName());
        evidence.setType(request.type() == null ? EvidenceType.TEXT : request.type());
        evidence.setFileUrl(request.fileUrl());
        evidence.setContent(request.content());
        TaskEvidence saved = evidenceRepository.save(evidence);
        return MarketplaceMapper.toEvidenceVO(saved);
    }

    /** 查看履约证据：仅双方或管理员。 */
    @Transactional(readOnly = true)
    public List<TaskEvidenceVO> evidences(Long id) {
        CurrentUser user = authService.requireUser();
        TaskContract contract = requireContract(id);
        requirePartyOrAdmin(user, contract, "非任务双方不能查看证据");
        return evidenceRepository.findByContractIdOrderByCreatedAtDesc(id).stream()
                .map(MarketplaceMapper::toEvidenceVO)
                .toList();
    }

    /** 猎人提交完成（事务）：契约与任务同步 WAIT_CONFIRM。 */
    @Transactional
    public TaskContractVO submitCompletion(Long id) {
        CurrentUser user = authService.requireUser();
        TaskContract contract = requireContract(id);
        if (!user.uid().equals(contract.getHunterId())) {
            throw new ForbiddenException("只有猎人可提交完成");
        }
        if (contract.getStatus() != ContractStatus.IN_PROGRESS) {
            throw new BusinessException(409, "当前契约不可提交完成");
        }
        Instant now = Instant.now();
        contract.setStatus(ContractStatus.WAIT_CONFIRM);
        contract.setSubmittedAt(now);
        contractRepository.save(contract);
        updateTaskStatus(contract.getTaskId(), TaskStatus.WAIT_CONFIRM);

        internalApiClient.createMessage(new CreateMessageRequest(
                contract.getPublisherId(),
                "CONTRACT",
                "猎人已提交完成",
                "《" + contract.getTaskTitle() + "》的猎人已提交完成，请确认",
                contract.getId()
        ));
        return MarketplaceMapper.toContractVO(contract);
    }

    /** 委托人确认完成（事务）：契约与任务同步 COMPLETED；事务后为猎人加信誉。 */
    @Transactional
    public TaskContractVO confirmCompletion(Long id) {
        CurrentUser user = authService.requireUser();
        TaskContract contract = requireContract(id);
        if (!user.uid().equals(contract.getPublisherId())) {
            throw new ForbiddenException("只有委托人可确认完成");
        }
        if (contract.getStatus() != ContractStatus.WAIT_CONFIRM) {
            throw new BusinessException(409, "当前契约不可确认完成");
        }
        Instant now = Instant.now();
        contract.setStatus(ContractStatus.COMPLETED);
        contract.setCompletedAt(now);
        contractRepository.save(contract);
        updateTaskStatus(contract.getTaskId(), TaskStatus.COMPLETED);

        // 旁路：为猎人加信誉 + 通知（失败不回滚主业务）
        internalApiClient.adjustCredit(contract.getHunterId(), 10, "CONTRACT_COMPLETE", contract.getId(), "完成任务");
        internalApiClient.createMessage(new CreateMessageRequest(
                contract.getHunterId(),
                "CONTRACT",
                "任务已确认完成",
                "《" + contract.getTaskTitle() + "》已确认完成，可以互相评价了",
                contract.getId()
        ));
        return MarketplaceMapper.toContractVO(contract);
    }

    /** 取消契约：仅双方或管理员；已完成/已裁决不可取消。 */
    @Transactional
    public TaskContractVO cancel(Long id, CancelContractRequest request) {
        CurrentUser user = authService.requireUser();
        TaskContract contract = requireContract(id);
        requirePartyOrAdmin(user, contract, "非任务双方不能取消契约");
        if (contract.getStatus() == ContractStatus.COMPLETED
                || contract.getStatus() == ContractStatus.RULED) {
            throw new BusinessException(409, "当前契约状态不可取消");
        }
        contract.setStatus(ContractStatus.CANCELLED);
        contract.setCancelReason(request == null ? null : request.cancelReason());
        contractRepository.save(contract);
        updateTaskStatus(contract.getTaskId(), TaskStatus.CANCELLED);
        return MarketplaceMapper.toContractVO(contract);
    }

    // ---------- 内部接口 ----------

    @Transactional(readOnly = true)
    public ContractBriefVO brief(Long contractId) {
        return MarketplaceMapper.toContractBrief(requireContract(contractId));
    }

    @Transactional(readOnly = true)
    public MarketplaceStatsVO stats() {
        long taskCount = taskRepository.count();
        long recruiting = taskRepository.countByStatus(TaskStatus.PUBLISHED);
        long completed = contractRepository.countByStatus(ContractStatus.COMPLETED);
        long contractCount = contractRepository.count();
        long dispute = contractRepository.countByStatus(ContractStatus.DISPUTED)
                + contractRepository.countByStatus(ContractStatus.RULED);
        return new MarketplaceStatsVO(taskCount, recruiting, completed, contractCount, dispute);
    }

    /** court 发起纠纷：契约 -> DISPUTED，任务 -> COURT_REVIEW。 */
    @Transactional
    public ContractBriefVO markDisputed(Long contractId, MarkDisputedRequest request) {
        TaskContract contract = requireContract(contractId);
        if (contract.getStatus() == ContractStatus.CANCELLED
                || contract.getStatus() == ContractStatus.COMPLETED
                || contract.getStatus() == ContractStatus.RULED) {
            throw new BusinessException(409, "当前契约状态不可发起纠纷");
        }
        contract.setStatus(ContractStatus.DISPUTED);
        contractRepository.save(contract);
        updateTaskStatus(contract.getTaskId(), TaskStatus.COURT_REVIEW);
        return MarketplaceMapper.toContractBrief(contract);
    }

    /** court 裁决结果：契约 -> RULED，任务 -> RULED。 */
    @Transactional
    public ContractBriefVO applyRuleResult(Long contractId, ContractRuleResultRequest request) {
        TaskContract contract = requireContract(contractId);
        contract.setStatus(ContractStatus.RULED);
        contractRepository.save(contract);
        updateTaskStatus(contract.getTaskId(), TaskStatus.RULED);
        return MarketplaceMapper.toContractBrief(contract);
    }

    /** reputation 评价权限检查。 */
    @Transactional(readOnly = true)
    public ReviewPermissionVO reviewPermission(Long contractId, Long userId) {
        TaskContract contract = requireContract(contractId);
        boolean isPublisher = userId != null && userId.equals(contract.getPublisherId());
        boolean isHunter = userId != null && userId.equals(contract.getHunterId());
        String role = isPublisher ? "PUBLISHER_TO_HUNTER" : "HUNTER_TO_PUBLISHER";
        Long revieweeId = isPublisher ? contract.getHunterId() : contract.getPublisherId();
        boolean allowed = (isPublisher || isHunter) && contract.getStatus() == ContractStatus.COMPLETED;
        return new ReviewPermissionVO(
                contract.getId(),
                contract.getTaskId(),
                contract.getTaskTitle(),
                contract.getPublisherId(),
                contract.getHunterId(),
                contract.getStatus().name(),
                userId,
                revieweeId,
                role,
                allowed
        );
    }

    /** reputation 评价成功后标记契约评价状态。契约须 COMPLETED。 */
    @Transactional
    public TaskContractVO markReviewFlag(Long contractId, ReviewFlagRequest request) {
        TaskContract contract = requireContract(contractId);
        if (contract.getStatus() != ContractStatus.COMPLETED) {
            throw new BusinessException(409, "契约完成后才能评价");
        }
        if ("PUBLISHER_TO_HUNTER".equals(request.role())) {
            contract.setReviewedByPublisher(true);
        } else if ("HUNTER_TO_PUBLISHER".equals(request.role())) {
            contract.setReviewedByHunter(true);
        }
        return MarketplaceMapper.toContractVO(contractRepository.save(contract));
    }

    // ---------- helpers ----------

    private TaskContract requireContract(Long id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("契约不存在"));
    }

    private void requirePartyOrAdmin(CurrentUser user, TaskContract contract, String message) {
        boolean party = user.uid().equals(contract.getPublisherId())
                || user.uid().equals(contract.getHunterId());
        if (!party && !authService.isAdmin(user)) {
            throw new ForbiddenException(message);
        }
    }

    private void updateTaskStatus(Long taskId, TaskStatus status) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task != null) {
            task.setStatus(status);
            taskRepository.save(task);
        }
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(page, size <= 0 ? 10 : size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
