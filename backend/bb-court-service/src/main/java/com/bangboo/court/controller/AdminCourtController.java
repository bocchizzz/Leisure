package com.bangboo.court.controller;

import com.bangboo.common.response.PageResult;
import com.bangboo.court.dto.CourtCaseDetailVO;
import com.bangboo.court.dto.CourtCaseVO;
import com.bangboo.court.dto.RuleCaseRequest;
import com.bangboo.court.service.CourtCaseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/court-cases")
public class AdminCourtController {
    private final CourtCaseService courtCaseService;

    public AdminCourtController(CourtCaseService courtCaseService) {
        this.courtCaseService = courtCaseService;
    }

    @GetMapping
    public PageResult<CourtCaseVO> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status
    ) {
        return courtCaseService.adminList(page, size, status);
    }

    @GetMapping("/{id}")
    public CourtCaseDetailVO getById(@PathVariable Long id) {
        return courtCaseService.adminGetById(id);
    }

    @PutMapping("/{id}/rule")
    public CourtCaseVO rule(@PathVariable Long id, @Valid @RequestBody RuleCaseRequest request) {
        return courtCaseService.rule(id, request);
    }
}
