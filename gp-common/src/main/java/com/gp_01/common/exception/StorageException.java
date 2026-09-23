package com.gp_01.common.exception;

import com.gp_01.common.enums.ErrorCode;

/**
 * 存储异常
 */
public class StorageException extends CommonException {

    public StorageException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public StorageException(Integer code, String msg, Throwable cause) {
        super(code, msg, cause);
    }

    public StorageException(Integer code, String msg) {
        super(code, msg);
    }

    public StorageException(ErrorCode errorCode) {
        super(errorCode);
    }
}
