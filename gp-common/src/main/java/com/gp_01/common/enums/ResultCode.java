package com.gp_01.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode{
    //请求成功
    SUCCESS(200, "操作成功"),
    //客户端错误
    UNAUTHORIZED(401, "未登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    //服务器错误
    SERVER_ERROR(500, "服务器异常");

    private final Integer code;
    private final String msg;


}
