package com.gp_01.gateway.config;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "gp.auth")
@Data
public class AuthProperties implements InitializingBean {

    private boolean enable = true;
    private List<String> excludePath = new ArrayList<>();

    public boolean getEnable(){
        return enable;
    }

    @Override
    public void afterPropertiesSet() {

        excludePath.add("/doc.html");
        excludePath.add("/v3/**");
        excludePath.add("/swagger-ui/**");
        excludePath.add("/swagger-resources/**");
        excludePath.add("/webjars/**");
        excludePath.add("/account/login");
        excludePath.add("/account/register");
    }
}
