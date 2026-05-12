package com.gp_01.common.autoconfig.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConditionalOnClass(BaseMapper.class)
public class MybatisConfig {


    @Bean
    public BaseMetaObjectHandler baseMetaObjectHandler(){
        return new BaseMetaObjectHandler();
    }
}
