package com.gp_01.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.net.InetAddress;
import java.net.UnknownHostException;
@EnableFeignClients(basePackages = {"com.gp_01.api.client"})
@SpringBootApplication
@Slf4j
public class GpAuthApplication {

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext run = SpringApplication.run(GpAuthApplication.class, args);
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
