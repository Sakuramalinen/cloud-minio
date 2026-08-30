package com.gp_01.file.service.config;

import com.gp_01.file.service.oss.OSS;
import com.gp_01.file.service.oss.download.Downloader;
import com.gp_01.file.service.oss.download.product.MinioDownloader;
import com.gp_01.file.service.oss.preview.Previewer;
import com.gp_01.file.service.oss.preview.product.MinioPreviewer;
import com.gp_01.file.service.oss.upload.Uploader;
import com.gp_01.file.service.oss.upload.product.MinioUploader;
import io.minio.MinioAsyncClient;
import io.minio.MinioClient;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
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

    @Bean
    public Uploader uploader(MinioUploader minioUploader){
        return minioUploader;
    }

    @Bean
    public Downloader downloader(MinioDownloader minioDownloader){
        return minioDownloader;
    }

    @Bean
    public Previewer previewer(MinioPreviewer minioPreviewer){
        return minioPreviewer;
    }


}
