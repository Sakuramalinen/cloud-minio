package com.gp_01.auth.service.config;

import com.gp_01.common.utils.RSAUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.interfaces.RSAPrivateKey;

@Configuration
@Data
@RequiredArgsConstructor
public class AuthConfig {


    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RSAPrivateKey privateKey(){
        String privateKey = RSAUtils.readPem("login_private_raw.pem");
        return RSAUtils.readPrivateKey(privateKey);
    }




}
