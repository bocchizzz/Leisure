package com.bangboo.common.exception;

import com.bangboo.common.constant.ErrorCode;

public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(String message) {
        this(ErrorCode.BAD_REQUEST, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
