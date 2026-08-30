package com.gp_01.authsdk_recourse.config;

import com.gp_01.authsdk_recourse.interceptors.LoginAuthInterceptor;
import com.gp_01.authsdk_recourse.interceptors.UploadInfoInterceptor;
import com.gp_01.authsdk_recourse.interceptors.UserInfoInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(ResourceAuthProperties.class)
@RequiredArgsConstructor
public class ResourceInterceptorConfig implements WebMvcConfigurer {

    private final ResourceAuthProperties resourceAuthProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //添加用户信息到ThreadLocal
        registry.addInterceptor(new UserInfoInterceptor()).order(0);
        //增加上传信息拦截器
        registry.addInterceptor(new UploadInfoInterceptor()).order(10);


        //判断是否需要做登录拦截
        if (!resourceAuthProperties.getEnable()) {
            return ;
        }
        //需要添加登录拦截
        InterceptorRegistration registration = registry.addInterceptor(new LoginAuthInterceptor()).order(1);

        //添加拦截路径
        if(resourceAuthProperties.getIncludePath() != null){
            registration.addPathPatterns(resourceAuthProperties.getIncludePath());
        }

        //添加排除路径
        if (resourceAuthProperties.getExcludePath() != null) {
            registration.excludePathPatterns(resourceAuthProperties.getExcludePath());
        }

        //添加默认排除路径
//        registration.excludePathPatterns("")

    }
}
