package com.bangboo.common.exception;

import com.bangboo.common.constant.ErrorCode;

public class ForbiddenException extends BusinessException {
    public ForbiddenException(String message) {
        super(ErrorCode.FORBIDDEN, message);
    }
}
