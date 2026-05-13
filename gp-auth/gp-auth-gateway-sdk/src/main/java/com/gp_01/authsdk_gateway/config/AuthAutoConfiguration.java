package com.gp_01.authsdk_gateway.config;

import com.gp_01.authsdk_gateway.utils.AuthUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class AuthAutoConfiguration {

    @Bean
    public AuthUtil authUtil(){
        return new AuthUtil();
    }
}
