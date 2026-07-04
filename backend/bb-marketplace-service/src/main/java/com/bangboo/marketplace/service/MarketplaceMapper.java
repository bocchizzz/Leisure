package com.bangboo.marketplace.service;

import com.bangboo.marketplace.dto.TaskApplicationVO;
import com.bangboo.marketplace.dto.TaskContractVO;
import com.bangboo.marketplace.dto.TaskEvidenceVO;
import com.bangboo.marketplace.dto.TaskVO;
import com.bangboo.marketplace.dto.internal.ContractBriefVO;
import com.bangboo.marketplace.entity.Task;
import com.bangboo.marketplace.entity.TaskApplication;
import com.bangboo.marketplace.entity.TaskContract;
import com.bangboo.marketplace.entity.TaskEvidence;

import java.util.Arrays;
import java.util.List;

/** 实体到 VO 的转换。VO 字段严格对齐前端 TS。 */
public final class MarketplaceMapper {
    private MarketplaceMapper() {
    }

    public static TaskVO toTaskVO(Task task, boolean favorited) {
        return new TaskVO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getCategory(),
                null,
                task.getDifficulty(),
                task.getBountyAmount(),
                task.getBountyType(),
                task.getLocation(),
                task.getDeadline(),
                task.getCompletionStandard(),
                task.getEvidenceRequirement(),
                task.getStatus(),
                null,
                task.getPublisherId(),
                task.getPublisherName(),
                task.getPublisherAvatar(),
                task.getAssignedHunterId(),
                task.getCoverImageUrl(),
                task.getApplicationCount(),
                favorited,
                task.getViewCount(),
                task.getSafetyStatus(),
                task.getSafetyReason(),
                splitCsv(task.getSafetyLabels()),
                task.getSafetyScore(),
                task.getCreatedAt(),
                task.getPublishedAt()
        );
    }

    public static TaskApplicationVO toApplicationVO(TaskApplication application) {
        return new TaskApplicationVO(
                application.getId(),
                application.getTaskId(),
                application.getTaskTitle(),
                application.getHunterId(),
                application.getHunterName(),
                application.getHunterAvatar(),
                application.getHunterLevel(),
                application.getHunterTitle(),
                application.getHunterReputation(),
                application.getApplyMessage(),
                application.getExpectedFinishTime(),
                application.getStatus(),
                application.getCreatedAt(),
                application.getUpdatedAt()
        );
    }

    public static TaskContractVO toContractVO(TaskContract contract) {
        return new TaskContractVO(
                contract.getId(),
                contract.getContractNo(),
                contract.getTaskId(),
                contract.getTaskTitle(),
                contract.getApplicationId(),
                contract.getPublisherId(),
                contract.getPublisherName(),
                contract.getPublisherAvatar(),
                contract.getHunterId(),
                contract.getHunterName(),
                contract.getHunterAvatar(),
                contract.getBountyAmount(),
                contract.getStatus(),
                contract.getCompletionStandard(),
                contract.getEvidenceRequirement(),
                contract.getReviewedByPublisher(),
                contract.getReviewedByHunter(),
                contract.getCancelReason(),
                contract.getAcceptedAt(),
                contract.getSubmittedAt(),
                contract.getCompletedAt(),
                contract.getCreatedAt()
        );
    }

    public static TaskEvidenceVO toEvidenceVO(TaskEvidence evidence) {
        return new TaskEvidenceVO(
                evidence.getId(),
                evidence.getContractId(),
                evidence.getTaskId(),
                evidence.getSubmitterId(),
                evidence.getSubmitterName(),
                evidence.getType(),
                evidence.getFileUrl(),
                evidence.getContent(),
                evidence.getCreatedAt()
        );
    }

    public static ContractBriefVO toContractBrief(TaskContract contract) {
        return new ContractBriefVO(
                contract.getId(),
                contract.getTaskId(),
                contract.getTaskTitle(),
                contract.getPublisherId(),
                contract.getPublisherName(),
                contract.getHunterId(),
                contract.getHunterName(),
                contract.getStatus().name(),
                contract.getBountyAmount(),
                contract.getBountyType().name()
        );
    }

    private static List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}
