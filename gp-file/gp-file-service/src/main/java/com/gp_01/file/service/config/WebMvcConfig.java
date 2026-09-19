package com.gp_01.file.service.config;

import com.gp_01.authsdk_recourse.interceptors.UploadInfoInterceptor;
import com.gp_01.file.service.intercepter.FileUploadInfoInterceptor;
import com.gp_01.file.service.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RedisUtils redisUtils;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {


        WebMvcConfigurer.super.addInterceptors(registry);
        registry.addInterceptor(new FileUploadInfoInterceptor(redisUtils)).order(10086);
    }
}
