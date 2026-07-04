package com.bangboo.reputation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * 猎人统计快照（rep_hunter_stats）。
 * 承载猎人主页与榜单所需的聚合数据，评价/完成/裁决时增量更新，
 * 榜单直接按此表排序，避免实时跨服务聚合。
 * userId 作为主键（一个用户一条统计）。
 */
@Entity
@Table(name = "rep_hunter_stats")
public class HunterStats {
    /** 用户 ID，作为主键。 */
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(length = 100)
    private String nickname;

    @Column(name = "avatar_url", length = 200)
    private String avatarUrl;

    /** 猎人等级 Lv0~Lv6。 */
    @Column(nullable = false)
    private Integer level;

    @Column(length = 50)
    private String title;

    /** 经验值。 */
    @Column(nullable = false)
    private Integer xp;

    /** 当前信誉分（与 IAM 同步的展示值）。 */
    @Column(nullable = false)
    private Integer reputation;

    @Column(name = "completed_task_count", nullable = false)
    private Integer completedTaskCount;

    /** 累计收到的好评数（rating>=4），用于 positiveRate。 */
    @Column(name = "positive_review_count", nullable = false)
    private Integer positiveReviewCount;

    /** 累计收到的评价总数，用于 positiveRate。 */
    @Column(name = "total_review_count", nullable = false)
    private Integer totalReviewCount;

    /** 准时完成次数，用于 onTimeRate（第一版按完成数近似）。 */
    @Column(name = "on_time_count", nullable = false)
    private Integer onTimeCount;

    /** 陪审被采纳次数。 */
    @Column(name = "arbitration_accepted_count", nullable = false)
    private Integer arbitrationAcceptedCount;

    /** 逗号分隔徽章 key。 */
    @Column(length = 500)
    private String badges;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getXp() {
        return xp;
    }

    public void setXp(Integer xp) {
        this.xp = xp;
    }

    public Integer getReputation() {
        return reputation;
    }

    public void setReputation(Integer reputation) {
        this.reputation = reputation;
    }

    public Integer getCompletedTaskCount() {
        return completedTaskCount;
    }

    public void setCompletedTaskCount(Integer completedTaskCount) {
        this.completedTaskCount = completedTaskCount;
    }

    public Integer getPositiveReviewCount() {
        return positiveReviewCount;
    }

    public void setPositiveReviewCount(Integer positiveReviewCount) {
        this.positiveReviewCount = positiveReviewCount;
    }

    public Integer getTotalReviewCount() {
        return totalReviewCount;
    }

    public void setTotalReviewCount(Integer totalReviewCount) {
        this.totalReviewCount = totalReviewCount;
    }

    public Integer getOnTimeCount() {
        return onTimeCount;
    }

    public void setOnTimeCount(Integer onTimeCount) {
        this.onTimeCount = onTimeCount;
    }

    public Integer getArbitrationAcceptedCount() {
        return arbitrationAcceptedCount;
    }

    public void setArbitrationAcceptedCount(Integer arbitrationAcceptedCount) {
        this.arbitrationAcceptedCount = arbitrationAcceptedCount;
    }

    public String getBadges() {
        return badges;
    }

    public void setBadges(String badges) {
        this.badges = badges;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
