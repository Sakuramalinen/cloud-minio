package com.gp_01.file.service;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
@Slf4j
@EnableFeignClients({"com.gp_01.api.client"})
@MapperScan("com.gp_01.file.service.mapper")
public class GpFileApplication {

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext run = SpringApplication.run(GpFileApplication.class, args);
        ConfigurableEnvironment environment = run.getEnvironment();
        String protocol = "http";
        log.info("--/\n----------------------------------------------------------------------\n\t" +
                "Application '{}' is running!\n\t" +
                "Local: \t\t{}://localhost:{}\n\t" +
                "External: \t{}://{}:{}\n\t" +
                "Profile(s): \t{}" +
                "\n----------------------------------------------------------------------",
                environment.getProperty("spring.application.name"),
                protocol,
                environment.getProperty("server.port"),
                protocol,
                //TODO 可能存在获取回环地址
                InetAddress.getLocalHost().getHostAddress(),
                environment.getProperty("server.port"),
                environment.getActiveProfiles()
                );


    }

}
