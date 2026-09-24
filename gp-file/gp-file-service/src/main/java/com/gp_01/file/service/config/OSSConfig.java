package com.gp_01.file.service.config;

import com.gp_01.file.service.oss.BucketManipulator;
import com.gp_01.file.service.oss.FileManipulator;
import com.gp_01.file.service.oss.OSS;
import com.gp_01.file.service.oss.impl.S3BucketManipulator;
import com.gp_01.file.service.oss.impl.S3FileManipulator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Data
@Slf4j
@Configuration
@ConfigurationProperties("gp.file.oss")
public class OSSConfig {

    private String url;

    private String region;

    private String accessKey;

    private String secretKey;

    private String defaultBucket;

    private String avatarBucket;

    private String tempBucket;


    @Bean
    public BucketManipulator bucketManipulator(S3Client s3Client){
        return new S3BucketManipulator(s3Client);
    }

    @Bean
    public FileManipulator fileManipulator(S3Client s3Client, S3Presigner s3Presigner){
        return new S3FileManipulator(s3Client, s3Presigner);
    }


    @Bean
    public OSS oss(BucketManipulator bucketManipulator, FileManipulator fileManipulator){
        return new OSS(url,region,accessKey,secretKey,defaultBucket,avatarBucket,tempBucket, bucketManipulator, fileManipulator);
    }


    @Bean
    public S3Client s3Client() {
        final String endpoint = "https://s3." + region + ".jdcloud-oss.com";

        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .build();
    }
    @Bean
    public S3Presigner s3Presigner() {
        final String endpoint = "https://s3." + region + ".jdcloud-oss.com";

        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
                .endpointOverride(URI.create(endpoint))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }



}
