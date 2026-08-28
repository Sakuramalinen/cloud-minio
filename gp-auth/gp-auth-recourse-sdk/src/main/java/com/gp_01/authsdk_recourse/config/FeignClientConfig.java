package com.gp_01.authsdk_recourse.config;

import com.gp_01.common.context.UserContext;
import com.gp_01.common.enums.RequestHeaderEnum;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor userContextFeignInterceptor(){
        return request -> {
            Long userId = UserContext.getUser();
            request.header(RequestHeaderEnum.LOGIN_AUTHORIZATION.getCustomHeaderName(), String.valueOf(userId));

        };
    }

}
