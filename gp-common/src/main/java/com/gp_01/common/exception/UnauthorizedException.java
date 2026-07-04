package com.gp_01.common.exception;


import com.gp_01.common.enums.ErrorCode;

/**
 * 未登录异常
 */
public class UnauthorizedException extends CommonException {


    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UnauthorizedException(Integer code, String msg) {
        super(code, msg);
    }

    public UnauthorizedException(CommonException e){
        super(e.getCode(), e.getMessage());
    }
}
