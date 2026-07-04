package com.bangboo.court.service;

import com.bangboo.court.dto.CourtCaseVO;
import com.bangboo.court.dto.CourtEvidenceVO;
import com.bangboo.court.dto.CourtPrecedentVO;
import com.bangboo.court.dto.CourtRulingVO;
import com.bangboo.court.dto.CourtStatementVO;
import com.bangboo.court.dto.CourtVoteVO;
import com.bangboo.court.dto.VoteStats;
import com.bangboo.court.entity.CourtCase;
import com.bangboo.court.entity.CourtEvidence;
import com.bangboo.court.entity.CourtPrecedent;
import com.bangboo.court.entity.CourtRuling;
import com.bangboo.court.entity.CourtStatement;
import com.bangboo.court.entity.CourtVote;

import java.util.Arrays;
import java.util.List;

public final class CourtMapper {
    private CourtMapper() {
    }

    public static CourtCaseVO toCaseVO(CourtCase courtCase, VoteStats voteStats, boolean myVoted) {
        return new CourtCaseVO(
                courtCase.getId(),
                courtCase.getCaseNo(),
                courtCase.getTaskId(),
                courtCase.getTaskTitle(),
                courtCase.getContractId(),
                courtCase.getCaseTitle(),
                courtCase.getType(),
                courtCase.getStatus(),
                courtCase.getPlaintiffId(),
                courtCase.getPlaintiffName(),
                courtCase.getDefendantId(),
                courtCase.getDefendantName(),
                courtCase.getInitialStatement(),
                courtCase.getSummary(),
                courtCase.getAiSummary(),
                courtCase.getAiEvidenceAnalysis(),
                courtCase.getAiRoast(),
                voteStats,
                myVoted,
                courtCase.getCreatedAt(),
                courtCase.getRuledAt()
        );
    }

    public static CourtStatementVO toStatementVO(CourtStatement statement) {
        return new CourtStatementVO(
                statement.getId(),
                statement.getCaseId(),
                statement.getUserId(),
                statement.getUserName(),
                statement.getUserAvatar(),
                statement.getRole(),
                statement.getContent(),
                statement.getCreatedAt()
        );
    }

    public static CourtEvidenceVO toEvidenceVO(CourtEvidence evidence) {
        return new CourtEvidenceVO(
                evidence.getId(),
                evidence.getCaseId(),
                evidence.getSubmitterId(),
                evidence.getSubmitterName(),
                evidence.getType(),
                evidence.getFileUrl(),
                evidence.getContent(),
                evidence.getCreatedAt()
        );
    }

    public static CourtVoteVO toVoteVO(CourtVote vote) {
        return new CourtVoteVO(
                vote.getId(),
                vote.getCaseId(),
                vote.getVoterId(),
                vote.getVoterName(),
                vote.getOption(),
                vote.getWeight(),
                vote.getReason(),
                Boolean.TRUE.equals(vote.getAdopted()),
                vote.getCreatedAt()
        );
    }

    public static CourtRulingVO toRulingVO(CourtRuling ruling) {
        if (ruling == null) {
            return null;
        }
        return new CourtRulingVO(
                ruling.getId(),
                ruling.getCaseId(),
                ruling.getAdminId(),
                ruling.getAdminName(),
                ruling.getResult(),
                ruling.getBountyReleaseRate(),
                ruling.getPublisherCreditDelta(),
                ruling.getHunterCreditDelta(),
                ruling.getReason(),
                ruling.getCreatedAt()
        );
    }

    public static CourtPrecedentVO toPrecedentVO(CourtPrecedent precedent) {
        List<String> tags = precedent.getTags() == null || precedent.getTags().isBlank()
                ? List.of()
                : Arrays.stream(precedent.getTags().split(","))
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .toList();
        return new CourtPrecedentVO(
                precedent.getId(),
                precedent.getCaseId(),
                precedent.getTitle(),
                precedent.getSummary(),
                precedent.getRulingResult(),
                tags,
                precedent.getCreatedAt()
        );
    }
}
