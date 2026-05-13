package com.gp_01.authsdk_recourse.interceptors.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "gp.auth")
@Data
public class AuthProperties {

    private boolean enable = true;
    private List<String> excludePath = null;

    public boolean getEnable(){
        return enable;
    }

}
