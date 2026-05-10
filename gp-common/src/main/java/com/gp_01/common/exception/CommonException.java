package com.gp_01.common.exception;

/**
 * 异常父类
 */
public class CommonException extends RuntimeException {
    public CommonException(String message) {
        super(message);
    }
}
