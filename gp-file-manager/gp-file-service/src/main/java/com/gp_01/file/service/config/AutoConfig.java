package com.gp_01.file.service.config;

import com.gp_01.file.service.operation.download.Downloader;
import com.gp_01.file.service.operation.download.product.MinioDownloader;
import io.minio.MinioClient;
import org.apache.tika.Tika;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AutoConfig {

    @Bean
    public Downloader downloader(MinioClient minioClient){
        return new MinioDownloader(minioClient);
    }

    @Bean
    public Tika tika (){
        return new Tika();
    }
}
