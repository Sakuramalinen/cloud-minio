package com.gp_01.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    SUCCESS(200, "ok"),
    //==================服务错误==================
    SERVICE_ERROR(10000, "服务内部异常"),

    REGISTER_ERROR(10100, "用户注册错误"),
    USER_EXIST_ERROR(10101,"用户已存在"),

    LOGIN_ERROR(10200, "用户未登录"),
    LOGIN_EXPIRATION_ERROR(10201, "登录过期"),


    AUTHORITY_ERROR(10300, "访问权限错误"),
    UNAUTHORIZED_ERROR(10301, "用户未授权"),
    AUTHORITY_EXPIRATION_ERROR(10311, "授权已过期"),

    PARAM_ERROR(10400, "用户请求参数异常"),

    BUSINESS_ERROR(10500, "业务异常"),


    RECOURSE_ERROR(10600, "用户资源异常"),
    RECOURSE_NOT_FOUND_ERROR(10601, "文件不存在"),
    RECOURSE_READ_ERROR(10610, "文件读取错误"),

    UPLOAD_FILE_ERROR(10700, "用户上传文件异常"),

    //==================系统错误==================
    SYSTEM_EXECUTE_ERROR(20000, "系统执行错误"),

    //==================调用第三方服务错误==================
    MIDDLEWARE_ERROR(20100, "中间件错误"),

    DB_ERROR(30300, "数据库错误");





    private final Integer code;
    private final String msg;
}
