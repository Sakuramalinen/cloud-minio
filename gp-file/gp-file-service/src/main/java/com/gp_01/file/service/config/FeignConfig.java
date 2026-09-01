package com.gp_01.file.service.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerlevel(){
        return Logger.Level.FULL;

    }
}
