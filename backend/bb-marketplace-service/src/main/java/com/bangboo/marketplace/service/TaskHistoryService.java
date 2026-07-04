package com.bangboo.marketplace.service;

import com.bangboo.common.response.PageResult;
import com.bangboo.common.security.CurrentUser;
import com.bangboo.marketplace.dto.TaskVO;
import com.bangboo.marketplace.entity.Task;
import com.bangboo.marketplace.entity.TaskHistory;
import com.bangboo.marketplace.repository.TaskHistoryRepository;
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
public class TaskHistoryService {
    private final TaskHistoryRepository historyRepository;
    private final TaskRepository taskRepository;
    private final MarketplaceAuthService authService;

    public TaskHistoryService(TaskHistoryRepository historyRepository,
                              TaskRepository taskRepository,
                              MarketplaceAuthService authService) {
        this.historyRepository = historyRepository;
        this.taskRepository = taskRepository;
        this.authService = authService;
    }

    /**
     * 分页查询当前用户的浏览历史。
     */
    @Transactional(readOnly = true)
    public PageResult<TaskVO> list(int page, int size) {
        CurrentUser user = authService.requireUser();
        Page<TaskHistory> pageResult = historyRepository
                .findByUserIdOrderByLastViewedAtDesc(user.uid(), PageRequest.of(page, size));

        Set<Long> taskIds = pageResult.getContent().stream()
                .map(TaskHistory::getTaskId)
                .collect(Collectors.toSet());
        List<Task> tasks = taskRepository.findAllById(taskIds);
        Map<Long, Task> taskMap = tasks.stream()
                .collect(Collectors.toMap(Task::getId, t -> t));

        return PageMapper.toPageResult(pageResult, history -> {
            Task task = taskMap.get(history.getTaskId());
            return task != null ? MarketplaceMapper.toTaskVO(task, false) : null;
        });
    }
}
