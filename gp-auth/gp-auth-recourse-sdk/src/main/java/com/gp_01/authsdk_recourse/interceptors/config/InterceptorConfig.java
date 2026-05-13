package com.gp_01.authsdk_recourse.interceptors.config;

import com.gp_01.authsdk_recourse.interceptors.LoginAuthInterceptor;
import com.gp_01.authsdk_recourse.interceptors.UserInfoInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class InterceptorConfig implements WebMvcConfigurer {

    private final AuthProperties authProperties;

    @Autowired
    public InterceptorConfig(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //添加用户信息到ThreadLocal
        registry.addInterceptor(new UserInfoInterceptor()).order(0);
        //需要添加登录校验
        if (authProperties.getEnable()) {
            InterceptorRegistration registration = registry.addInterceptor(new LoginAuthInterceptor()).order(1);
            if(authProperties.getExcludePath() != null){
                registration.excludePathPatterns(authProperties.getExcludePath());
            }
        }
    }
}
