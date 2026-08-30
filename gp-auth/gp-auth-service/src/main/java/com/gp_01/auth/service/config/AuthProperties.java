package com.gp_01.auth.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gp.auth")
@Data
public class AuthProperties {

    //记住我可以记住几秒 单位：秒
    private long expire;


}
