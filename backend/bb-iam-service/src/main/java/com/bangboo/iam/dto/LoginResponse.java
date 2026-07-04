package com.bangboo.iam.dto;

public record LoginResponse(String token, UserVO user) {
}
