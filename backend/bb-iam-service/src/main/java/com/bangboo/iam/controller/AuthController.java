package com.bangboo.iam.controller;

import com.bangboo.common.security.CurrentUser;
import com.bangboo.iam.dto.LoginRequest;
import com.bangboo.iam.dto.LoginResponse;
import com.bangboo.iam.dto.RegisterRequest;
import com.bangboo.iam.dto.UserVO;
import com.bangboo.iam.service.CurrentUserService;
import com.bangboo.iam.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final CurrentUserService currentUserService;

    public AuthController(UserService userService, CurrentUserService currentUserService) {
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @PostMapping("/register")
    public UserVO register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/logout")
    public Void logout() {
        currentUserService.requireUser();
        return null;
    }

    @GetMapping("/me")
    public UserVO me() {
        CurrentUser user = currentUserService.requireUser();
        return userService.getProfile(user.uid());
    }
}
