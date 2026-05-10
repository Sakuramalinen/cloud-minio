package com.gp_01.common.exception;

/**
 * 拒绝异常，权限不足异常
 */
public class ForbiddenException extends CommonException {
    public ForbiddenException(String message) {
        super(message);
    }
}
