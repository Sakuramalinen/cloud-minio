package com.gp_01.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
@Data
@Configuration
@ConfigurationProperties(prefix = "gp.user")
public class UserConfig {

    private Long DefaultUserStore;

}
