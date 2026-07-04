package com.bangboo.iam.controller;

import com.bangboo.iam.dto.IamStatsVO;
import com.bangboo.iam.dto.ReputationAdjustmentRequest;
import com.bangboo.iam.dto.ReputationAdjustmentVO;
import com.bangboo.iam.dto.UserAccessStateVO;
import com.bangboo.iam.dto.UserBriefVO;
import com.bangboo.iam.dto.UserBriefsRequest;
import com.bangboo.iam.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/users")
public class InternalUserController {
    private final UserService userService;

    public InternalUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}/brief")
    public UserBriefVO brief(@PathVariable Long id) {
        return userService.getBrief(id);
    }

    @PostMapping("/briefs")
    public List<UserBriefVO> briefs(@RequestBody UserBriefsRequest request) {
        return userService.getBriefs(request.userIds());
    }

    @GetMapping("/{id}/access-state")
    public UserAccessStateVO accessState(@PathVariable Long id) {
        return userService.getAccessState(id);
    }

    @PostMapping("/{id}/reputation-adjustments")
    public ReputationAdjustmentVO adjustReputation(
            @PathVariable Long id,
            @Valid @RequestBody ReputationAdjustmentRequest request
    ) {
        return userService.adjustReputation(id, request);
    }

    @GetMapping("/stats")
    public IamStatsVO stats() {
        return userService.stats();
    }
}
