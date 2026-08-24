package com.gp_01.common.exception;

import com.gp_01.common.enums.ErrorCode;
import lombok.Getter;

/**
 * 异常父类
 */
@Getter
public class CommonException extends RuntimeException {

    private final Integer code;

    public CommonException(ErrorCode errorCode, Throwable cause){
        super(errorCode.getMsg(), cause);
        code = errorCode.getCode();
    }

    public CommonException(Integer code, String msg, Throwable cause){
        super(msg, cause);
        this.code = code;
    }

    public CommonException(Integer code, String msg){
        super(msg);
        this.code = code;
    }

    public CommonException(ErrorCode errorCode){
        super(errorCode.getMsg());
        code = errorCode.getCode();
    }





}
