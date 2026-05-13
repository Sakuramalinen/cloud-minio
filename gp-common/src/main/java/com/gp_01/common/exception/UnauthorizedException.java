package com.gp_01.common.exception;



/**
 * 未登录异常
 */
public class UnauthorizedException extends CommonException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
