package com.bangboo.marketplace.controller;

import com.bangboo.common.response.PageResult;
import com.bangboo.marketplace.dto.CancelTaskRequest;
import com.bangboo.marketplace.dto.CreateApplicationRequest;
import com.bangboo.marketplace.dto.CreateTaskRequest;
import com.bangboo.marketplace.dto.TaskApplicationVO;
import com.bangboo.marketplace.dto.TaskQuery;
import com.bangboo.marketplace.dto.TaskVO;
import com.bangboo.marketplace.service.ApplicationService;
import com.bangboo.marketplace.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;
    private final ApplicationService applicationService;

    public TaskController(TaskService taskService, ApplicationService applicationService) {
        this.taskService = taskService;
        this.applicationService = applicationService;
    }

    @GetMapping
    public PageResult<TaskVO> list(TaskQuery query) {
        return taskService.list(query);
    }

    @GetMapping("/my-published")
    public PageResult<TaskVO> myPublished(TaskQuery query) {
        return taskService.myPublished(query);
    }

    @GetMapping("/my-accepted")
    public PageResult<TaskVO> myAccepted(TaskQuery query) {
        return taskService.myAccepted(query);
    }

    @GetMapping("/recommendations")
    public List<TaskVO> recommendations() {
        return taskService.recommendations();
    }

    @GetMapping("/{id}")
    public TaskVO getById(@PathVariable Long id) {
        return taskService.getById(id);
    }

    @PostMapping
    public TaskVO create(@Valid @RequestBody CreateTaskRequest request) {
        return taskService.create(request);
    }

    @PutMapping("/{id}")
    public TaskVO update(@PathVariable Long id, @Valid @RequestBody CreateTaskRequest request) {
        return taskService.update(id, request);
    }

    @PutMapping("/{id}/cancel")
    public TaskVO cancel(@PathVariable Long id, @RequestBody(required = false) CancelTaskRequest request) {
        String reason = request == null ? null : request.reason();
        return taskService.cancel(id, reason);
    }

    @PostMapping("/{taskId}/applications")
    public TaskApplicationVO apply(@PathVariable Long taskId, @RequestBody(required = false) CreateApplicationRequest request) {
        CreateApplicationRequest body = request == null ? new CreateApplicationRequest(null, null) : request;
        return applicationService.apply(taskId, body);
    }

    @GetMapping("/{taskId}/applications")
    public List<TaskApplicationVO> applications(@PathVariable Long taskId) {
        return applicationService.listByTask(taskId);
    }
}
