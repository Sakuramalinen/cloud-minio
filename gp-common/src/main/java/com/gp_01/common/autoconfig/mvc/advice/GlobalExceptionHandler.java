package com.gp_01.common.autoconfig.mvc.advice;

import com.gp_01.common.domain.Result;
import com.gp_01.common.enums.ResultCode;
import com.gp_01.common.exception.CommonException;
import com.gp_01.common.exception.ForbiddenException;
import com.gp_01.common.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Component
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ForbiddenException.class)
    public Result<Void> ForbiddenExceptionHandle(ForbiddenException e){
        log.error("拒绝异常:", e);
        return Result.error(ResultCode.FORBIDDEN, e.getMessage());
    }
    @ExceptionHandler(UnauthorizedException.class)
    public Result<Void> UnauthorizedExceptionHandle(UnauthorizedException e){
        log.error("未登录异常", e);
        return Result.error(ResultCode.UNAUTHORIZED);
    }

    @ExceptionHandler(CommonException.class)
    public Result<Void> CommonExceptionHandle(CommonException e){
        log.error("自定义异常信息:", e);
        return Result.error("非法请求" + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> ExceptionHandle(Exception e){
        log.error("服务器内部异常：", e);
        return Result.error(ResultCode.SERVER_ERROR);
    }


}
