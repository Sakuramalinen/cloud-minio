package com.gp_01.common.exception;

import com.gp_01.common.enums.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 拒绝异常，权限不足异常
 */
public class ForbiddenException extends CommonException {

    public ForbiddenException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public ForbiddenException(Integer code, String msg, Throwable cause) {
        super(code, msg, cause);
    }

    public ForbiddenException(Integer code, String msg) {
        super(code, msg);
    }

    public ForbiddenException(ErrorCode errorCode) {
        super(errorCode);
    }
}
