package com.gp_01.file.service.config.oss;

import com.gp_01.file.service.oss.OSS;
import io.minio.MinioAsyncClient;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final OSS oss;

    @Bean
    public MinioClient minioClient() {
        return new MinioClient.Builder()
                .endpoint(oss.getUrl())
                .credentials(oss.getAccessKey(), oss.getSecretKey())
                .build();
    }

    @Bean
    public MinioAsyncClient minioAsyncClient() {
        return new MinioAsyncClient.Builder()
                .endpoint(oss.getUrl())
                .credentials(oss.getAccessKey(), oss.getSecretKey())
                .build();
    }

}
