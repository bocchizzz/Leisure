package com.bangboo.reputation.repository;

import com.bangboo.reputation.entity.HunterStats;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HunterStatsRepository extends JpaRepository<HunterStats, Long> {
    /** 榜单：按经验值降序，其次信誉分降序。 */
    List<HunterStats> findAllByOrderByXpDescReputationDesc(Pageable pageable);

    /** 榜单：按信誉分降序。 */
    List<HunterStats> findAllByOrderByReputationDescXpDesc(Pageable pageable);
}
