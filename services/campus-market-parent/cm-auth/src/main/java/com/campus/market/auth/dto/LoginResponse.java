package com.campus.market.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录响应
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    
    private String token;
    private UserVO user;
}
