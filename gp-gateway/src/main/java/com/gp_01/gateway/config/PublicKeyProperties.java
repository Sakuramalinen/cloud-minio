package com.gp_01.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gp.gateway")
@Data
public class PublicKeyProperties {

    private String loginPublicKey;

    private String ossPublicKey;
}
