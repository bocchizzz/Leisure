package com.bangboo.court.dto;

public record VoteStats(
        double supportPublisherRate,
        double supportHunterRate,
        double insufficientEvidenceRate,
        double settlementRate,
        int totalVotes,
        int totalWeight
) {
}
