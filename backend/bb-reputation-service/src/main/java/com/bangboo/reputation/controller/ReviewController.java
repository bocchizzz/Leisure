package com.bangboo.reputation.controller;

import com.bangboo.reputation.dto.ContractReviewVO;
import com.bangboo.reputation.dto.CreateReviewRequest;
import com.bangboo.reputation.dto.ReviewVO;
import com.bangboo.reputation.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 评价对外接口。对齐前端 review.ts。
 * 路径不含 /api 前缀，由 Gateway 转发。
 */
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /** 提交评价。评价方向由后端根据当前用户与契约双方判断。 */
    @PostMapping
    public ReviewVO create(@Valid @RequestBody CreateReviewRequest request) {
        return reviewService.create(request);
    }

    /** 用户收到的公开评价（可匿名访问）。 */
    @GetMapping("/user/{userId}")
    public List<ReviewVO> byUser(@PathVariable Long userId) {
        return reviewService.byUser(userId);
    }

    /** 任务评价（需相关方或管理员）。 */
    @GetMapping("/task/{taskId}")
    public List<ReviewVO> byTask(@PathVariable Long taskId) {
        return reviewService.byTask(taskId);
    }

    /** 契约双方评价汇总。 */
    @GetMapping("/contract/{contractId}")
    public ContractReviewVO byContract(@PathVariable Long contractId) {
        return reviewService.byContract(contractId);
    }
}
