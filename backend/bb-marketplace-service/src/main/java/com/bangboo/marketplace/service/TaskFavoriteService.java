package com.bangboo.marketplace.service;

import com.bangboo.common.exception.BusinessException;
import com.bangboo.common.response.PageResult;
import com.bangboo.common.security.CurrentUser;
import com.bangboo.marketplace.dto.TaskVO;
import com.bangboo.marketplace.entity.Task;
import com.bangboo.marketplace.entity.TaskFavorite;
import com.bangboo.marketplace.repository.TaskFavoriteRepository;
import com.bangboo.marketplace.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TaskFavoriteService {
    private final TaskFavoriteRepository favoriteRepository;
    private final TaskRepository taskRepository;
    private final MarketplaceAuthService authService;
    private final IdGenerator idGenerator;

    public TaskFavoriteService(TaskFavoriteRepository favoriteRepository,
                               TaskRepository taskRepository,
                               MarketplaceAuthService authService,
                               IdGenerator idGenerator) {
        this.favoriteRepository = favoriteRepository;
        this.taskRepository = taskRepository;
        this.authService = authService;
        this.idGenerator = idGenerator;
    }

    /**
     * 分页查询当前用户的收藏任务列表。
     */
    @Transactional(readOnly = true)
    public PageResult<TaskVO> list(int page, int size) {
        CurrentUser user = authService.requireUser();
        Page<TaskFavorite> pageResult = favoriteRepository
                .findByUserIdOrderByCreatedAtDesc(user.uid(), PageRequest.of(page, size));

        Set<Long> taskIds = pageResult.getContent().stream()
                .map(TaskFavorite::getTaskId)
                .collect(Collectors.toSet());
        List<Task> tasks = taskRepository.findAllById(taskIds);
        Map<Long, Task> taskMap = tasks.stream()
                .collect(Collectors.toMap(Task::getId, t -> t));

        return PageMapper.toPageResult(pageResult, fav -> {
            Task task = taskMap.get(fav.getTaskId());
            return task != null ? MarketplaceMapper.toTaskVO(task, true) : null;
        });
    }

    /**
     * 添加任务收藏（幂等，已收藏则直接返回成功）。
     */
    @Transactional
    public void add(Long taskId) {
        CurrentUser user = authService.requireUser();
        taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(404, "任务不存在"));

        if (favoriteRepository.existsByUserIdAndTaskId(user.uid(), taskId)) {
            return;
        }

        TaskFavorite favorite = new TaskFavorite();
        favorite.setId(idGenerator.nextFavoriteId());
        favorite.setUserId(user.uid());
        favorite.setTaskId(taskId);
        favoriteRepository.save(favorite);
    }

    /**
     * 取消任务收藏。
     */
    @Transactional
    public void remove(Long taskId) {
        CurrentUser user = authService.requireUser();
        if (!favoriteRepository.existsByUserIdAndTaskId(user.uid(), taskId)) {
            throw new BusinessException(404, "尚未收藏该任务");
        }
        favoriteRepository.deleteByUserIdAndTaskId(user.uid(), taskId);
    }
}
