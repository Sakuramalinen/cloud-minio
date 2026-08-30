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
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("gp.file.oss")
@Data
public class OSSConfig {
    private String bucketName;
    private String url;
    private String avatarBucketName;
    private String tempBucketName;
    private String accessKey;
    private String secretKey;





    @Bean
    public OSS oss(){
        return new OSS(bucketName, url, avatarBucketName, tempBucketName, accessKey, secretKey);
    }




}
