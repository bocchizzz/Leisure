package com.bangboo.iam.controller;

import com.bangboo.common.security.CurrentUser;
import com.bangboo.iam.dto.CertificationVO;
import com.bangboo.iam.dto.CreateCertificationRequest;
import com.bangboo.iam.service.CertificationService;
import com.bangboo.iam.service.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/certifications")
public class CertificationController {
    private final CertificationService certificationService;
    private final CurrentUserService currentUserService;

    public CertificationController(CertificationService certificationService, CurrentUserService currentUserService) {
        this.certificationService = certificationService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public CertificationVO submit(@Valid @RequestBody CreateCertificationRequest request) {
        CurrentUser user = currentUserService.requireUser();
        return certificationService.submit(user.uid(), request);
    }

    @GetMapping("/me")
    public CertificationVO me() {
        CurrentUser user = currentUserService.requireUser();
        return certificationService.latestMine(user.uid());
    }
}
