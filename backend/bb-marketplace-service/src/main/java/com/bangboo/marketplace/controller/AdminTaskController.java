package com.bangboo.marketplace.controller;

import com.bangboo.common.response.PageResult;
import com.bangboo.marketplace.dto.ReviewTaskRequest;
import com.bangboo.marketplace.dto.TaskQuery;
import com.bangboo.marketplace.dto.TaskVO;
import com.bangboo.marketplace.service.TaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/tasks")
public class AdminTaskController {
    private final TaskService taskService;

    public AdminTaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public PageResult<TaskVO> list(TaskQuery query) {
        return taskService.adminList(query);
    }

    @PutMapping("/{id}/review")
    public TaskVO review(@PathVariable Long id, @RequestBody ReviewTaskRequest request) {
        return taskService.reviewTask(id, request);
    }
}
