package com.bangboo.marketplace.service;

import com.bangboo.common.exception.BusinessException;
import com.bangboo.common.exception.ForbiddenException;
import com.bangboo.common.exception.ResourceNotFoundException;
import com.bangboo.common.response.PageResult;
import com.bangboo.common.security.CurrentUser;
import com.bangboo.marketplace.client.InternalApiClient;
import com.bangboo.marketplace.dto.CreateTaskRequest;
import com.bangboo.marketplace.dto.TaskQuery;
import com.bangboo.marketplace.dto.TaskVO;
import com.bangboo.marketplace.dto.ReviewTaskRequest;
import com.bangboo.marketplace.dto.internal.CreateAuditLogRequest;
import com.bangboo.marketplace.dto.internal.CreateMessageRequest;
import com.bangboo.marketplace.dto.internal.UserBriefVO;
import com.bangboo.marketplace.entity.Task;
import com.bangboo.marketplace.entity.TaskFavorite;
import com.bangboo.marketplace.entity.TaskHistory;
import com.bangboo.marketplace.enums.TaskStatus;
import com.bangboo.marketplace.repository.TaskFavoriteRepository;
import com.bangboo.marketplace.repository.TaskHistoryRepository;
import com.bangboo.marketplace.repository.TaskRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private static final Set<String> VALID_DIFFICULTIES = Set.of("F", "E", "D", "C", "B", "A", "S");

    private final TaskRepository taskRepository;
    private final TaskFavoriteRepository favoriteRepository;
    private final TaskHistoryRepository historyRepository;
    private final MarketplaceAuthService authService;
    private final SafetyChecker safetyChecker;
    private final InternalApiClient internalApiClient;
    private final IdGenerator idGenerator;

    public TaskService(
            TaskRepository taskRepository,
            TaskFavoriteRepository favoriteRepository,
            TaskHistoryRepository historyRepository,
            MarketplaceAuthService authService,
            SafetyChecker safetyChecker,
            InternalApiClient internalApiClient,
            IdGenerator idGenerator
    ) {
        this.taskRepository = taskRepository;
        this.favoriteRepository = favoriteRepository;
        this.historyRepository = historyRepository;
        this.authService = authService;
        this.safetyChecker = safetyChecker;
        this.internalApiClient = internalApiClient;
        this.idGenerator = idGenerator;
    }

    /** 任务大厅：公开，默认只查 PUBLISHED，可按条件筛选。 */
    @Transactional(readOnly = true)
    public PageResult<TaskVO> list(TaskQuery query) {
        TaskStatus status = query.getStatus() == null ? TaskStatus.PUBLISHED : query.getStatus();
        Specification<Task> spec = buildSpec(query, status);
        Pageable pageable = buildPageable(query.getPage(), query.getSize(), query.getSort());
        Page<Task> page = taskRepository.findAll(spec, pageable);
        Set<Long> favoriteTaskIds = currentUserFavoriteIds();
        return PageMapper.toPageResult(page, task -> MarketplaceMapper.toTaskVO(task, favoriteTaskIds.contains(task.getId())));
    }

    /** 任务详情：匿名仅 PUBLISHED；委托人/已选猎人/管理员可看非公开；登录时写浏览历史（失败不影响）。 */
    @Transactional
    public TaskVO getById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));
        Optional<CurrentUser> currentUser = authService.optionalUser();

        if (!isPubliclyVisible(task)) {
            CurrentUser user = currentUser.orElseThrow(() -> new ForbiddenException("无权查看该任务"));
            boolean allowed = authService.isAdmin(user)
                    || user.uid().equals(task.getPublisherId())
                    || user.uid().equals(task.getAssignedHunterId());
            if (!allowed) {
                throw new ForbiddenException("无权查看该任务");
            }
        }

        boolean favorited = false;
        if (currentUser.isPresent()) {
            Long uid = currentUser.get().uid();
            favorited = favoriteRepository.existsByUserIdAndTaskId(uid, task.getId());
            recordHistorySafely(uid, task.getId());
        }
        return MarketplaceMapper.toTaskVO(task, favorited);
    }

    /** 发布任务：登录 + 校园认证；内容安全校验；状态 PENDING_REVIEW；保存委托人快照。 */
    @Transactional
    public TaskVO create(CreateTaskRequest request) {
        CurrentUser user = authService.requireActiveVerifiedUser();
        validateDifficulty(request.difficulty());
        SafetyChecker.SafetyResult safety = safetyChecker.checkTaskContent(request.title(), request.description());

        Task task = new Task();
        Long taskId = idGenerator.nextTaskId();
        task.setId(taskId);
        task.setTaskNo("TASK-" + taskId);
        applyRequest(task, request);
        task.setStatus(TaskStatus.PENDING_REVIEW);
        task.setPublisherId(user.uid());
        applyPublisherSnapshot(task, user.uid());
        task.setApplicationCount(0);
        task.setViewCount(0);
        task.setSafetyStatus(safety.status());
        task.setSafetyScore(safety.score());
        task.setSafetyReason(safety.reason());
        Task saved = taskRepository.save(task);

        return MarketplaceMapper.toTaskVO(saved, false);
    }

    /** 编辑任务：仅发布者；编辑后状态回到 PENDING_REVIEW。 */
    @Transactional
    public TaskVO update(Long id, CreateTaskRequest request) {
        CurrentUser user = authService.requireUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));
        if (!user.uid().equals(task.getPublisherId())) {
            throw new ForbiddenException("只有发布者可以编辑任务");
        }
        if (task.getStatus() == TaskStatus.IN_PROGRESS
                || task.getStatus() == TaskStatus.WAIT_CONFIRM
                || task.getStatus() == TaskStatus.COMPLETED
                || task.getStatus() == TaskStatus.DISPUTED
                || task.getStatus() == TaskStatus.COURT_REVIEW
                || task.getStatus() == TaskStatus.RULED) {
            throw new BusinessException(409, "当前任务状态不可编辑");
        }
        validateDifficulty(request.difficulty());
        SafetyChecker.SafetyResult safety = safetyChecker.checkTaskContent(request.title(), request.description());
        applyRequest(task, request);
        task.setStatus(TaskStatus.PENDING_REVIEW);
        task.setSafetyStatus(safety.status());
        task.setSafetyScore(safety.score());
        task.setSafetyReason(safety.reason());
        Task saved = taskRepository.save(task);
        return MarketplaceMapper.toTaskVO(saved, false);
    }

    /** 取消任务：仅发布者。 */
    @Transactional
    public TaskVO cancel(Long id, String reason) {
        CurrentUser user = authService.requireUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));
        if (!user.uid().equals(task.getPublisherId())) {
            throw new ForbiddenException("只有发布者可以取消任务");
        }
        if (task.getStatus() == TaskStatus.COMPLETED || task.getStatus() == TaskStatus.RULED) {
            throw new BusinessException(409, "当前任务状态不可取消");
        }
        task.setStatus(TaskStatus.CANCELLED);
        task.setCancelReason(reason);
        Task saved = taskRepository.save(task);
        return MarketplaceMapper.toTaskVO(saved, false);
    }

    @Transactional(readOnly = true)
    public PageResult<TaskVO> myPublished(TaskQuery query) {
        CurrentUser user = authService.requireUser();
        Pageable pageable = buildPageable(query.getPage(), query.getSize(), query.getSort());
        Page<Task> page = taskRepository.findByPublisherId(user.uid(), pageable);
        return PageMapper.toPageResult(page, task -> MarketplaceMapper.toTaskVO(task, false));
    }

    @Transactional(readOnly = true)
    public PageResult<TaskVO> myAccepted(TaskQuery query) {
        CurrentUser user = authService.requireUser();
        Pageable pageable = buildPageable(query.getPage(), query.getSize(), query.getSort());
        Page<Task> page = taskRepository.findByAssignedHunterId(user.uid(), pageable);
        return PageMapper.toPageResult(page, task -> MarketplaceMapper.toTaskVO(task, false));
    }

    @Transactional(readOnly = true)
    public List<TaskVO> recommendations() {
        Set<Long> favoriteTaskIds = currentUserFavoriteIds();
        Pageable top = PageRequest.of(0, 6);
        return taskRepository.findByStatusOrderByCreatedAtDesc(TaskStatus.PUBLISHED, top).stream()
                .map(task -> MarketplaceMapper.toTaskVO(task, favoriteTaskIds.contains(task.getId())))
                .toList();
    }

    /** 管理端任务列表：管理员。 */
    @Transactional(readOnly = true)
    public PageResult<TaskVO> adminList(TaskQuery query) {
        authService.requireAdmin();
        Specification<Task> spec = buildSpec(query, query.getStatus());
        Pageable pageable = buildPageable(query.getPage(), query.getSize(), query.getSort());
        Page<Task> page = taskRepository.findAll(spec, pageable);
        return PageMapper.toPageResult(page, task -> MarketplaceMapper.toTaskVO(task, false));
    }

    /** 管理端审核任务：approved -> PUBLISHED；否则 REMOVED。写审计 + 通知发布者。 */
    @Transactional
    public TaskVO reviewTask(Long id, ReviewTaskRequest request) {
        CurrentUser admin = authService.requireAdmin();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));
        boolean approved = Boolean.TRUE.equals(request.approved());
        if (approved) {
            task.setStatus(TaskStatus.PUBLISHED);
            task.setPublishedAt(Instant.now());
            task.setSafetyStatus(com.bangboo.marketplace.enums.SafetyStatus.PASS);
        } else {
            task.setStatus(TaskStatus.REMOVED);
        }
        task.setReviewComment(request.comment());
        Task saved = taskRepository.save(task);

        // 旁路：审计 + 消息（失败不回滚）
        internalApiClient.createAuditLog(new CreateAuditLogRequest(
                admin.uid(),
                approved ? "TASK_APPROVE" : "TASK_REJECT",
                "TASK",
                saved.getId(),
                approved ? "管理员审核通过任务" : "管理员驳回任务"
        ));
        internalApiClient.createMessage(new CreateMessageRequest(
                saved.getPublisherId(),
                "TASK",
                approved ? "任务审核通过" : "任务审核未通过",
                "《" + saved.getTitle() + "》" + (approved ? "已发布" : "未通过审核"),
                saved.getId()
        ));
        return MarketplaceMapper.toTaskVO(saved, false);
    }

    // ---------- helpers ----------

    private Specification<Task> buildSpec(TaskQuery query, TaskStatus status) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (query.getCategory() != null) {
                predicates.add(cb.equal(root.get("category"), query.getCategory()));
            }
            if (query.getDifficulty() != null && !query.getDifficulty().isBlank()) {
                predicates.add(cb.equal(root.get("difficulty"), query.getDifficulty()));
            }
            if (query.getBountyType() != null) {
                predicates.add(cb.equal(root.get("bountyType"), query.getBountyType()));
            }
            if (query.getMinBounty() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("bountyAmount"), query.getMinBounty()));
            }
            if (query.getMaxBounty() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("bountyAmount"), query.getMaxBounty()));
            }
            if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
                String like = "%" + query.getKeyword().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Pageable buildPageable(int page, int size, String sort) {
        Sort sortSpec = Sort.by(Sort.Direction.DESC, "createdAt");
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            String property = parts[0].trim();
            Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim())
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            if (!property.isBlank()) {
                sortSpec = Sort.by(direction, property);
            }
        }
        return PageRequest.of(page, size, sortSpec);
    }

    private boolean isPubliclyVisible(Task task) {
        return task.getStatus() == TaskStatus.PUBLISHED;
    }

    private Set<Long> currentUserFavoriteIds() {
        return authService.optionalUser()
                .map(user -> favoriteRepository
                        .findByUserIdOrderByCreatedAtDesc(user.uid(), PageRequest.of(0, 1000))
                        .stream()
                        .map(TaskFavorite::getTaskId)
                        .collect(Collectors.toSet()))
                .orElseGet(Set::of);
    }

    private void recordHistorySafely(Long userId, Long taskId) {
        try {
            Optional<TaskHistory> existing = historyRepository.findByUserIdAndTaskId(userId, taskId);
            if (existing.isPresent()) {
                TaskHistory history = existing.get();
                history.setViewCount(history.getViewCount() + 1);
                history.setLastViewedAt(Instant.now());
                historyRepository.save(history);
            } else {
                TaskHistory history = new TaskHistory();
                history.setId(idGenerator.nextHistoryId());
                history.setUserId(userId);
                history.setTaskId(taskId);
                history.setViewCount(1);
                history.setLastViewedAt(Instant.now());
                historyRepository.save(history);
            }
        } catch (RuntimeException ignored) {
            // Writing browse history must not break detail retrieval.
        }
    }

    private void applyRequest(Task task, CreateTaskRequest request) {
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setCategory(request.category());
        task.setDifficulty(request.difficulty());
        task.setBountyAmount(request.bountyAmount());
        task.setBountyType(request.bountyType());
        task.setLocation(request.location());
        task.setDeadline(request.deadline());
        task.setCompletionStandard(request.completionStandard());
        task.setEvidenceRequirement(request.evidenceRequirement());
        task.setCoverImageUrl(request.coverImageUrl());
    }

    private void applyPublisherSnapshot(Task task, Long publisherId) {
        Optional<UserBriefVO> brief = internalApiClient.userBrief(publisherId);
        if (brief.isPresent()) {
            UserBriefVO user = brief.get();
            task.setPublisherName(user.nickname() != null ? user.nickname() : user.username());
            task.setPublisherAvatar(user.avatarUrl());
        } else {
            task.setPublisherName("用户" + publisherId);
            task.setPublisherAvatar("bangboo:kacha");
        }
    }

    private void validateDifficulty(String difficulty) {
        if (difficulty == null || !VALID_DIFFICULTIES.contains(difficulty.trim().toUpperCase())) {
            throw new BusinessException(400, "任务难度非法，应为 F/E/D/C/B/A/S 之一");
        }
    }
}
