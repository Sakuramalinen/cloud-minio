package com.gp_01.authsdk_gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gp.auth.jwt")
@Data
public class JWTProperties {

    private String secret;
    //单位：秒
    private long expire;
}
