package com.bangboo.iam.controller;

import com.bangboo.common.security.CurrentUser;
import com.bangboo.iam.dto.UpdateProfileRequest;
import com.bangboo.iam.dto.UserVO;
import com.bangboo.iam.service.CurrentUserService;
import com.bangboo.iam.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final CurrentUserService currentUserService;

    public UserController(UserService userService, CurrentUserService currentUserService) {
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/{id}")
    public UserVO getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @GetMapping("/profile")
    public UserVO profile() {
        CurrentUser user = currentUserService.requireUser();
        return userService.getProfile(user.uid());
    }

    @PutMapping("/profile")
    public UserVO updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        CurrentUser user = currentUserService.requireUser();
        return userService.updateProfile(user.uid(), request);
    }
}
