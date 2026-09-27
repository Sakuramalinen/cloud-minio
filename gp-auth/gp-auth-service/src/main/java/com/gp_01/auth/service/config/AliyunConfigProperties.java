package com.gp_01.auth.service.config;

import com.aliyun.auth.credentials.Credential;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Data
@Configuration
@ConfigurationProperties("gp.auth.aliyun")
public class AliyunConfigProperties {


    private String accessKeyId;

    private String accessKeySecret;




}
