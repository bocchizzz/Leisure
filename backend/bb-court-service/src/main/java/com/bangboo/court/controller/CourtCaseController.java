package com.bangboo.court.controller;

import com.bangboo.common.response.PageResult;
import com.bangboo.court.dto.CourtCaseDetailVO;
import com.bangboo.court.dto.CourtCaseVO;
import com.bangboo.court.dto.CourtEvidenceVO;
import com.bangboo.court.dto.CourtStatementVO;
import com.bangboo.court.dto.CourtVoteVO;
import com.bangboo.court.dto.CreateCourtCaseRequest;
import com.bangboo.court.dto.CreateCourtEvidenceRequest;
import com.bangboo.court.dto.CreateStatementRequest;
import com.bangboo.court.dto.CreateVoteRequest;
import com.bangboo.court.dto.VoteStats;
import com.bangboo.court.service.CourtCaseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/court-cases")
public class CourtCaseController {
    private final CourtCaseService courtCaseService;

    public CourtCaseController(CourtCaseService courtCaseService) {
        this.courtCaseService = courtCaseService;
    }

    @PostMapping
    public CourtCaseVO create(@Valid @RequestBody CreateCourtCaseRequest request) {
        return courtCaseService.create(request);
    }

    @GetMapping
    public PageResult<CourtCaseVO> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status
    ) {
        return courtCaseService.list(page, size, status);
    }

    @GetMapping("/{id}")
    public CourtCaseDetailVO getById(@PathVariable Long id) {
        return courtCaseService.getById(id);
    }

    @PostMapping("/{id}/statements")
    public CourtStatementVO addStatement(@PathVariable Long id, @Valid @RequestBody CreateStatementRequest request) {
        return courtCaseService.addStatement(id, request);
    }

    @PostMapping("/{id}/evidences")
    public CourtEvidenceVO addEvidence(@PathVariable Long id, @Valid @RequestBody CreateCourtEvidenceRequest request) {
        return courtCaseService.addEvidence(id, request);
    }

    @PostMapping("/{id}/votes")
    public CourtVoteVO vote(@PathVariable Long id, @Valid @RequestBody CreateVoteRequest request) {
        return courtCaseService.vote(id, request);
    }

    @GetMapping("/{id}/votes/stats")
    public VoteStats voteStats(@PathVariable Long id) {
        return courtCaseService.voteStats(id);
    }
}
