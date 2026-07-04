package com.bangboo.common.exception;

import com.bangboo.common.constant.ErrorCode;

public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }
}
