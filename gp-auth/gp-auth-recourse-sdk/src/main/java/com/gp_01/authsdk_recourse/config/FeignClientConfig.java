package com.gp_01.authsdk_recourse.config;

import com.gp_01.common.context.UserContext;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.gp_01.common.constants.AuthConstants.USER_INFO_HEADER;

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor userContextFeignInterceptor(){
        return request -> {
            Long userId = UserContext.getUser();
            request.header(USER_INFO_HEADER, String.valueOf(userId));

        };
    }

}
