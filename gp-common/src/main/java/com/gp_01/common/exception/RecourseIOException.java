package com.gp_01.common.exception;

import com.gp_01.common.enums.ErrorCode;

/**
 * 资源未找到
 */
public class RecourseIOException extends CommonException {


    public RecourseIOException(ErrorCode errorCode) {
        super(errorCode);
    }

    public RecourseIOException(Integer code, String msg) {
        super(code, msg);
    }
}
