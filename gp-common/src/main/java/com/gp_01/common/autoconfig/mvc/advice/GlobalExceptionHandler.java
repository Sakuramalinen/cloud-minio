package com.gp_01.common.autoconfig.mvc.advice;

import com.gp_01.common.domain.Result;
import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Component
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ForbiddenException.class)
    public Result<?> ForbiddenExceptionHandle(ForbiddenException e){
        log.error("权限异常:", e);
        return Result.error(e.getCode(),e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public Result<?> UnauthorizedExceptionHandle(UnauthorizedException e){
        log.warn("未登录异常", e);
        return Result.error(e.getCode(),e.getMessage());
    }

    @ExceptionHandler(RecourseIOException.class)
    public Result<?> RecourseNotFoundExceptionHandler(RecourseIOException e){
        log.warn("资源未找到异常", e);
        return Result.error(e.getCode(),e.getMessage());

    }
    @ExceptionHandler(BadRequestException.class)
    public Result<?> BadRequestExceptionHandler(BadRequestException e){
        log.error("请求错误异常", e);
        return Result.error(e.getCode(),e.getMessage());

    }

    @ExceptionHandler(CommonException.class)
    public Result<?> CommonExceptionHandle(CommonException e){
        log.error("自定义异常信息:", e);
        return Result.error(e.getCode(),e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> ExceptionHandle(Exception e){
        log.error("服务器内部异常：", e);
        return Result.error(ErrorCode.SERVICE_ERROR);
    }


}
