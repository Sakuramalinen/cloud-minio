package com.gp_01.common.exception;

import com.gp_01.common.enums.ErrorCode;

/**
 * 资源未找到
 */
public class RecourseIOException extends CommonException {

    public RecourseIOException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public RecourseIOException(Integer code, String msg, Throwable cause) {
        super(code, msg, cause);
    }

    public RecourseIOException(Integer code, String msg) {
        super(code, msg);
    }

    public RecourseIOException(ErrorCode errorCode) {
        super(errorCode);
    }
}
