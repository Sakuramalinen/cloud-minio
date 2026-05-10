package com.gp_01.common.autoconfig.mybatis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisConfig {


    @Bean
    public BaseMetaObjectHandler baseMetaObjectHandler(){
        return new BaseMetaObjectHandler();
    }
}
