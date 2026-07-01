package com.gp_01.common.exception;

/**
 * 凭证过期异常
 */
public class PrivilegeExpirationException extends ExpirationException{
    public PrivilegeExpirationException(String message) {
        super(message);
    }
}
