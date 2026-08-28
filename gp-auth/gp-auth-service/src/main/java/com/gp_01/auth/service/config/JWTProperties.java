package com.gp_01.auth.service.config;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gp.auth")
@Data
public class JWTProperties {

    //记住我可以记住几秒 单位：秒
    private long expire;

}
