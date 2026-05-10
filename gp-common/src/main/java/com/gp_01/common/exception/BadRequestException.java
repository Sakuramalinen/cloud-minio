package com.gp_01.common.exception;

/**
 * 请求不合法异常
 */
public class BadRequestException extends CommonException {
    public BadRequestException(String message) {
        super(message);
    }
}
