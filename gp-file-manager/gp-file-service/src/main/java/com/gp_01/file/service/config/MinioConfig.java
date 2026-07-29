package com.gp_01.file.service.config;

import io.minio.MinioAsyncClient;
import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "gp.minio")
public class MinioConfig {
    private String url;
    private String bucketName;
    private String tempBucketName;
    private String accessKey;
    private String secretKey;
    private Integer tempSignedUrlExpireMinute;

    @Bean
    public MinioClient minioClient() {
        return new MinioClient.Builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }
    @Bean
    public MinioAsyncClient minioAsyncClient() {
        return new MinioAsyncClient.Builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }


}
