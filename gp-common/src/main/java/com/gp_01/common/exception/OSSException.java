package com.gp_01.common.exception;

import com.gp_01.common.enums.ErrorCode;

/**
 * 对象存储服务异常
 */
public class OSSException extends CommonException {


  public OSSException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public OSSException(Integer code, String msg, Throwable cause) {
    super(code, msg, cause);
  }

  public OSSException(Integer code, String msg) {
    super(code, msg);
  }

  public OSSException(ErrorCode errorCode) {
    super(errorCode);
  }
}
