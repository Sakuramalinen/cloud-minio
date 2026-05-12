package com.gp_01.gateway.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Set;

//@Component
@Data
@Configuration
@ConfigurationProperties(prefix = "pg.auth")
public class AuthProperties implements InitializingBean {
    private Set<String> excludePath;

    @Override
    public void afterPropertiesSet() throws Exception {
        //默认放行路径
        excludePath.add("/accounts/login");
        excludePath.add("/accounts/register");
    }
}
