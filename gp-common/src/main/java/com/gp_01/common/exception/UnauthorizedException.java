package com.gp_01.common.exception;


import com.gp_01.common.enums.ErrorCode;

/**
 * 未登录异常
 */
public class UnauthorizedException extends CommonException {

    public UnauthorizedException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public UnauthorizedException(Integer code, String msg, Throwable cause) {
        super(code, msg, cause);
    }

    public UnauthorizedException(Integer code, String msg) {
        super(code, msg);
    }

    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
