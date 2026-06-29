package com.campus.market.auth.controller;

import com.campus.market.common.response.ApiResponse;
import com.campus.market.auth.repository.UserRepository;
import com.campus.market.auth.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class PublicStatsController {
    
    private final UserRepository userRepository;
    
    @GetMapping("/users")
    public ApiResponse<Map<String, Long>> getUserStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", userRepository.count());
        stats.put("active", userRepository.countByStatus(UserStatus.ACTIVE));
        return ApiResponse.success(stats);
    }
}
