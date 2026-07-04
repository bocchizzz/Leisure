package com.bangboo.court.dto;

public record ContractBriefVO(
        Long id,
        Long taskId,
        String taskTitle,
        Long publisherId,
        String publisherName,
        Long hunterId,
        String hunterName,
        String status,
        Double bountyAmount,
        String bountyType
) {
}
