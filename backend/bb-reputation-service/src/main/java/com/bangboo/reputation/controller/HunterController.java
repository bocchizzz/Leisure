package com.bangboo.reputation.controller;

import com.bangboo.common.response.PageResult;
import com.bangboo.reputation.dto.CreditLogVO;
import com.bangboo.reputation.dto.HunterProfileVO;
import com.bangboo.reputation.dto.LeaderboardEntryVO;
import com.bangboo.reputation.service.HunterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 猎人档案 / 榜单 / 信誉对外接口。对齐前端 hunter.ts。
 * 注意路由顺序：/hunters/me、/hunters/leaderboard 为精确路径，优先于 /hunters/{userId}。
 */
@RestController
@RequestMapping("/hunters")
public class HunterController {
    private final HunterService hunterService;

    public HunterController(HunterService hunterService) {
        this.hunterService = hunterService;
    }

    /** 我的猎人档案。 */
    @GetMapping("/me")
    public HunterProfileVO me() {
        return hunterService.me();
    }

    /** 公会榜单（公开），返回数组。 */
    @GetMapping("/leaderboard")
    public List<LeaderboardEntryVO> leaderboard(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer limit
    ) {
        return hunterService.leaderboard(type, limit);
    }

    /** 猎人主页（公开）。 */
    @GetMapping("/{userId}")
    public HunterProfileVO getById(@PathVariable Long userId) {
        return hunterService.getById(userId);
    }

    /** 用户徽章（公开）。 */
    @GetMapping("/{userId}/badges")
    public List<String> badges(@PathVariable Long userId) {
        return hunterService.badges(userId);
    }

    /** 信誉变化记录（分页）。 */
    @GetMapping("/{userId}/credit-logs")
    public PageResult<CreditLogVO> creditLogs(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return hunterService.creditLogs(userId, page, size);
    }
}
