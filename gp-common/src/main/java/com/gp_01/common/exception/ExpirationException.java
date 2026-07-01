package com.gp_01.common.exception;

/**
 * 过期异常
 */
public class ExpirationException extends RuntimeException {
    public ExpirationException(String message) {
        super(message);
    }
}
