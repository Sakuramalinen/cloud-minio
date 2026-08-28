package com.gp_01.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RequestHeaderEnum {
    LOGIN_AUTHORIZATION("Authorization", "user-info"),
    OSS_AUTHORIZATION("OSS", "oss");


    //解析前的请求头
    private final String requestHeaderName;
    //解析后的请求头
    private final String customHeaderName;
}
