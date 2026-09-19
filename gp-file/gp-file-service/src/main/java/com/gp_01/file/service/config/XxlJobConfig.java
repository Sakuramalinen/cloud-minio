package com.gp_01.file.service.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "gp.xxl-job")
public class XxlJobConfig {

    private String adminAddress;

    private String accessToken;

    private String appName;

    private int port;

    private String logPath;

    private int logRetentionDay;

    @Bean
    public XxlJobSpringExecutor xxlJobSpringExecutor(){
        log.info(">>>>>>>> xxl-job executor start");
        XxlJobSpringExecutor executor = new XxlJobSpringExecutor();
        executor.setAdminAddresses(adminAddress);
        executor.setAccessToken(accessToken);
        executor.setAppname(appName);
        executor.setPort(port);
        executor.setLogPath(logPath);
        executor.setLogRetentionDays(logRetentionDay);
        return executor;
    }


}
