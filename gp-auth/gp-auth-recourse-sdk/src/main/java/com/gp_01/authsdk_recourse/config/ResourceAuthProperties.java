package com.gp_01.authsdk_recourse.config;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "gp.auth.resource")
public class ResourceAuthProperties implements InitializingBean {
    private Boolean enable = true;
    private List<String> includePath = new ArrayList<>();
    private List<String> excludePath = new ArrayList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        excludePath.add("/doc.html");
        excludePath.add("/v3/**");
        excludePath.add("/swagger-ui/**");
        excludePath.add("/swagger-resources/**");
        excludePath.add("/webjars/**");
    }
}
