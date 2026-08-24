package com.gp_01.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.List;

@Configuration
public class CorsConfig {

    private static final List<String> allowedOrigins = List.of("http://localhost:5173");
    private static final List<String> allowedHeaders = List.of("*");
    private static final List<String> allowedMethods = List.of("GET", "POST", "DELETE", "PUT", "OPTIONS");

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedHeaders(allowedHeaders);
        config.setAllowedMethods(allowedMethods);
        config.setAllowCredentials(true);
        config.setMaxAge(Duration.ofHours(1));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",config);

        return new CorsWebFilter(source);
    }
}
