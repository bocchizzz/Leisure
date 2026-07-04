package com.bangboo.court.controller;

import com.bangboo.court.dto.CourtCaseDetailVO;
import com.bangboo.court.service.CourtCaseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 小法庭内部接口，供其他服务通过 X-Internal-Token 调用。
 * 目前提供案件完整资料查询，供 AI 服务生成案件摘要/点评。
 */
@RestController
@RequestMapping("/internal/court-cases")
public class CourtInternalController {
    private final CourtCaseService courtCaseService;

    public CourtInternalController(CourtCaseService courtCaseService) {
        this.courtCaseService = courtCaseService;
    }

    @GetMapping("/{id}")
    public CourtCaseDetailVO internalGetById(@PathVariable Long id) {
        return courtCaseService.internalGetById(id);
    }
}
