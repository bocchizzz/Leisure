package com.bangboo.iam.controller;

import com.bangboo.common.response.PageResult;
import com.bangboo.common.security.CurrentUser;
import com.bangboo.iam.dto.BanUserRequest;
import com.bangboo.iam.dto.CertificationVO;
import com.bangboo.iam.dto.ReviewCertificationRequest;
import com.bangboo.iam.dto.UserVO;
import com.bangboo.iam.service.CertificationService;
import com.bangboo.iam.service.CurrentUserService;
import com.bangboo.iam.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminUserController {
    private final UserService userService;
    private final CertificationService certificationService;
    private final CurrentUserService currentUserService;

    public AdminUserController(
            UserService userService,
            CertificationService certificationService,
            CurrentUserService currentUserService
    ) {
        this.userService = userService;
        this.certificationService = certificationService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/users")
    public PageResult<UserVO> users(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status
    ) {
        currentUserService.requireAdmin();
        return userService.adminList(page, size, keyword, status);
    }

    @PutMapping("/users/{id}/ban")
    public UserVO banUser(@PathVariable Long id, @RequestBody(required = false) BanUserRequest request) {
        currentUserService.requireAdmin();
        return userService.banUser(id, request == null ? new BanUserRequest(null) : request);
    }

    @PutMapping("/users/{id}/unban")
    public UserVO unbanUser(@PathVariable Long id) {
        currentUserService.requireAdmin();
        return userService.unbanUser(id);
    }

    @GetMapping("/certifications")
    public PageResult<CertificationVO> certifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status
    ) {
        currentUserService.requireAdmin();
        return certificationService.adminList(page, size, status);
    }

    @PutMapping("/certifications/{id}/review")
    public CertificationVO reviewCertification(
            @PathVariable Long id,
            @RequestBody ReviewCertificationRequest request
    ) {
        CurrentUser user = currentUserService.requireAdmin();
        return certificationService.review(id, user.uid(), request);
    }
}
