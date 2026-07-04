package com.gp_01.common.exception;

import com.gp_01.common.enums.ErrorCode;

/**
 * 业务异常
 */
public class BadRequestException extends CommonException {

    public BadRequestException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BadRequestException(Integer code, String msg) {
        super(code, msg);
    }

}
