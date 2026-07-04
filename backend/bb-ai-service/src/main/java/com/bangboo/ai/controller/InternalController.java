package com.bangboo.ai.controller;

import com.bangboo.ai.dto.internal.AiStatsVO;
import com.bangboo.ai.service.AiPersistenceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ai 对内部服务提供的接口，仅允许携带 X-Internal-Token 的服务间调用。
 * - stats/ai：供 admin-ops 看板。
 */
@RestController
@RequestMapping("/internal")
public class InternalController {
    private final AiPersistenceService persistence;

    public InternalController(AiPersistenceService persistence) {
        this.persistence = persistence;
    }

    @GetMapping("/stats/ai")
    public AiStatsVO stats() {
        return new AiStatsVO(persistence.callCount());
    }
}
