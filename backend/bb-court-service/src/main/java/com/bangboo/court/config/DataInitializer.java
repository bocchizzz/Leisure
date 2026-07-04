package com.bangboo.court.config;

import com.bangboo.court.entity.CourtCase;
import com.bangboo.court.entity.CourtEvidence;
import com.bangboo.court.entity.CourtPrecedent;
import com.bangboo.court.entity.CourtStatement;
import com.bangboo.court.entity.CourtVote;
import com.bangboo.court.enums.CourtCaseStatus;
import com.bangboo.court.enums.CourtCaseType;
import com.bangboo.court.enums.CourtPartyRole;
import com.bangboo.court.enums.CourtVoteOption;
import com.bangboo.court.enums.EvidenceType;
import com.bangboo.court.enums.RulingResult;
import com.bangboo.court.repository.CourtCaseRepository;
import com.bangboo.court.repository.CourtEvidenceRepository;
import com.bangboo.court.repository.CourtPrecedentRepository;
import com.bangboo.court.repository.CourtRulingRepository;
import com.bangboo.court.repository.CourtStatementRepository;
import com.bangboo.court.repository.CourtVoteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class DataInitializer {
    private static final long SEED_CASE_ID = 401L;
    private static final String SEED_CASE_NO = "COURT-2026-0401";

    @Bean
    CommandLineRunner seedCourtData(
            CourtCaseRepository caseRepository,
            CourtStatementRepository statementRepository,
            CourtEvidenceRepository evidenceRepository,
            CourtVoteRepository voteRepository,
            CourtRulingRepository rulingRepository,
            CourtPrecedentRepository precedentRepository,
            TransactionTemplate transactionTemplate
    ) {
        return args -> transactionTemplate.executeWithoutResult(status -> {
            if (caseRepository.existsById(SEED_CASE_ID)) {
                return;
            }

            caseRepository.findByCaseNo(SEED_CASE_NO).filter(existing -> existing.getId() != SEED_CASE_ID).ifPresent(existing -> {
                clearCaseChildren(existing.getId(), statementRepository, evidenceRepository, voteRepository, rulingRepository, precedentRepository);
                caseRepository.delete(existing);
                caseRepository.flush();
            });

            CourtCase courtCase = new CourtCase();
            courtCase.setId(SEED_CASE_ID);
            courtCase.setCaseNo(SEED_CASE_NO);
            courtCase.setTaskId(201L);
            courtCase.setTaskTitle("Poster polishing and layout");
            courtCase.setContractId(301L);
            courtCase.setCaseTitle("Poster delivery quality dispute");
            courtCase.setType(CourtCaseType.QUALITY_DISPUTE);
            courtCase.setStatus(CourtCaseStatus.VOTING);
            courtCase.setPlaintiffId(2L);
            courtCase.setPlaintiffName("Client One");
            courtCase.setDefendantId(3L);
            courtCase.setDefendantName("Hunter One");
            courtCase.setInitialStatement("The delivered poster still contains visible typos.");
            courtCase.setSummary("The parties disagree about revision scope and delivery quality.");
            courtCase.setAiSummary("The dispute focuses on whether unlimited revisions were included and whether the final draft met acceptance criteria.");
            courtCase.setAiEvidenceAnalysis("Current evidence includes typo lists, revision records and delivery screenshots.");
            caseRepository.save(courtCase);

            CourtStatement plaintiffStatement = new CourtStatement();
            plaintiffStatement.setCaseId(SEED_CASE_ID);
            plaintiffStatement.setUserId(2L);
            plaintiffStatement.setUserName("Client One");
            plaintiffStatement.setUserAvatar("bangboo:kacha");
            plaintiffStatement.setRole(CourtPartyRole.PLAINTIFF);
            plaintiffStatement.setContent("The draft does not meet publishing requirements and still has text errors.");
            statementRepository.save(plaintiffStatement);

            CourtStatement defendantStatement = new CourtStatement();
            defendantStatement.setCaseId(SEED_CASE_ID);
            defendantStatement.setUserId(3L);
            defendantStatement.setUserName("Hunter One");
            defendantStatement.setUserAvatar("bangboo:kacha");
            defendantStatement.setRole(CourtPartyRole.DEFENDANT);
            defendantStatement.setContent("I completed two revision rounds as agreed; extra changes exceed the original scope.");
            statementRepository.save(defendantStatement);

            CourtEvidence textEvidence = new CourtEvidence();
            textEvidence.setCaseId(SEED_CASE_ID);
            textEvidence.setSubmitterId(2L);
            textEvidence.setSubmitterName("Client One");
            textEvidence.setType(EvidenceType.TEXT);
            textEvidence.setContent("Typo list: title, subtitle and activity location.");
            evidenceRepository.save(textEvidence);

            CourtEvidence imageEvidence = new CourtEvidence();
            imageEvidence.setCaseId(SEED_CASE_ID);
            imageEvidence.setSubmitterId(3L);
            imageEvidence.setSubmitterName("Hunter One");
            imageEvidence.setType(EvidenceType.IMAGE);
            imageEvidence.setFileUrl("/uploads/mock/evidence-poster.png");
            imageEvidence.setContent("Screenshots after two revision rounds.");
            evidenceRepository.save(imageEvidence);

            CourtVote vote = new CourtVote();
            vote.setCaseId(SEED_CASE_ID);
            vote.setVoterId(4L);
            vote.setVoterName("Hunter Two");
            vote.setOption(CourtVoteOption.SUPPORT_HUNTER);
            vote.setWeight(1);
            vote.setReason("The original scope did not include unlimited revisions.");
            voteRepository.save(vote);

            if (precedentRepository.count() == 0) {
                CourtPrecedent precedent = new CourtPrecedent();
                precedent.setCaseId(SEED_CASE_ID);
                precedent.setTitle("Revision scope and acceptance criteria");
                precedent.setSummary("When unlimited revisions are not explicitly agreed, the original scope and revision count should be key evidence.");
                precedent.setRulingResult(RulingResult.PARTIAL_HUNTER);
                precedent.setTags("quality dispute,revision scope,evidence");
                precedentRepository.save(precedent);
            }
        });
    }

    private static void clearCaseChildren(
            Long caseId,
            CourtStatementRepository statementRepository,
            CourtEvidenceRepository evidenceRepository,
            CourtVoteRepository voteRepository,
            CourtRulingRepository rulingRepository,
            CourtPrecedentRepository precedentRepository
    ) {
        statementRepository.deleteByCaseId(caseId);
        evidenceRepository.deleteByCaseId(caseId);
        voteRepository.deleteByCaseId(caseId);
        rulingRepository.deleteByCaseId(caseId);
        precedentRepository.deleteByCaseId(caseId);
    }
}
