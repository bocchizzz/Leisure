package com.bangboo.marketplace.controller;

import com.bangboo.common.response.PageResult;
import com.bangboo.marketplace.dto.TaskVO;
import com.bangboo.marketplace.service.TaskHistoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/task-history")
public class TaskHistoryController {
    private final TaskHistoryService historyService;

    public TaskHistoryController(TaskHistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public PageResult<TaskVO> list(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        return historyService.list(page, size);
    }
}
