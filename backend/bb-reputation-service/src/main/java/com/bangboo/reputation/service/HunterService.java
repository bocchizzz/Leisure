package com.bangboo.reputation.service;

import com.bangboo.common.exception.ResourceNotFoundException;
import com.bangboo.common.response.PageResult;
import com.bangboo.common.security.CurrentUser;
import com.bangboo.reputation.dto.CreditLogVO;
import com.bangboo.reputation.dto.HunterProfileVO;
import com.bangboo.reputation.dto.LeaderboardEntryVO;
import com.bangboo.reputation.entity.CreditLog;
import com.bangboo.reputation.entity.HunterStats;
import com.bangboo.reputation.repository.CreditLogRepository;
import com.bangboo.reputation.repository.HunterStatsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class HunterService {
    private final HunterStatsRepository hunterStatsRepository;
    private final CreditLogRepository creditLogRepository;
    private final CreditService creditService;
    private final ReputationAuthService authService;

    public HunterService(
            HunterStatsRepository hunterStatsRepository,
            CreditLogRepository creditLogRepository,
            CreditService creditService,
            ReputationAuthService authService
    ) {
        this.hunterStatsRepository = hunterStatsRepository;
        this.creditLogRepository = creditLogRepository;
        this.creditService = creditService;
        this.authService = authService;
    }

    /** 猎人主页（公开）。若无统计记录则按需初始化。 */
    @Transactional
    public HunterProfileVO getById(Long userId) {
        HunterStats stats = creditService.getOrCreateStats(userId);
        return ReputationMapper.toHunterProfileVO(stats);
    }

    /** 我的猎人档案。 */
    @Transactional
    public HunterProfileVO me() {
        CurrentUser user = authService.requireUser();
        HunterStats stats = creditService.getOrCreateStats(user.uid());
        return ReputationMapper.toHunterProfileVO(stats);
    }

    /** 公会榜单（公开），返回数组。type=xp（默认）按经验，type=reputation 按信誉。 */
    @Transactional(readOnly = true)
    public List<LeaderboardEntryVO> leaderboard(String type, Integer limit) {
        int size = (limit == null || limit <= 0) ? 20 : Math.min(limit, 100);
        Pageable pageable = PageRequest.of(0, size);
        List<HunterStats> list = "reputation".equalsIgnoreCase(type)
                ? hunterStatsRepository.findAllByOrderByReputationDescXpDesc(pageable)
                : hunterStatsRepository.findAllByOrderByXpDescReputationDesc(pageable);
        List<LeaderboardEntryVO> result = new ArrayList<>();
        int rank = 1;
        for (HunterStats s : list) {
            result.add(ReputationMapper.toLeaderboardEntry(s, rank++));
        }
        return result;
    }

    /** 用户徽章（公开），返回字符串数组。 */
    @Transactional
    public List<String> badges(Long userId) {
        HunterStats stats = creditService.getOrCreateStats(userId);
        return ReputationMapper.splitCsv(stats.getBadges());
    }

    /** 信誉变化记录（分页）。 */
    @Transactional(readOnly = true)
    public PageResult<CreditLogVO> creditLogs(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size <= 0 ? 10 : size);
        Page<CreditLog> result = creditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return PageMapper.toPageResult(result, ReputationMapper::toCreditLogVO);
    }
}
