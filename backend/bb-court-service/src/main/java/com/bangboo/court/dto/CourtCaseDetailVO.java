package com.bangboo.court.dto;

import java.util.List;

public record CourtCaseDetailVO(
        CourtCaseVO courtCase,
        List<CourtStatementVO> statements,
        List<CourtEvidenceVO> evidences,
        CourtRulingVO ruling
) {
}
