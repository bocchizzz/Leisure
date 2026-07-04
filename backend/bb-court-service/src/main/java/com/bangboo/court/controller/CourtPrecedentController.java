package com.bangboo.court.controller;

import com.bangboo.common.response.PageResult;
import com.bangboo.court.dto.CourtPrecedentVO;
import com.bangboo.court.service.CourtCaseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/court-precedents")
public class CourtPrecedentController {
    private final CourtCaseService courtCaseService;

    public CourtPrecedentController(CourtCaseService courtCaseService) {
        this.courtCaseService = courtCaseService;
    }

    @GetMapping
    public PageResult<CourtPrecedentVO> precedents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        return courtCaseService.precedents(page, size, keyword);
    }
}
