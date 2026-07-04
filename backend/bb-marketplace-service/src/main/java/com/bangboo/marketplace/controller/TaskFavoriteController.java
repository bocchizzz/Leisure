package com.bangboo.marketplace.controller;

import com.bangboo.common.response.PageResult;
import com.bangboo.marketplace.dto.TaskVO;
import com.bangboo.marketplace.service.TaskFavoriteService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/task-favorites")
public class TaskFavoriteController {
    private final TaskFavoriteService favoriteService;

    public TaskFavoriteController(TaskFavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public PageResult<TaskVO> list(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        return favoriteService.list(page, size);
    }

    @PostMapping("/{taskId}")
    public void add(@PathVariable Long taskId) {
        favoriteService.add(taskId);
    }

    @DeleteMapping("/{taskId}")
    public void remove(@PathVariable Long taskId) {
        favoriteService.remove(taskId);
    }
}
