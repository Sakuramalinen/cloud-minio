package com.gp_01.file.service.oss.impl;

import com.gp_01.file.service.oss.BucketManipulator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;

@Slf4j
@RequiredArgsConstructor
public class S3BucketManipulator implements BucketManipulator {

    private final S3Client s3Client;

    @Override
    public void createBucketIfNotExist(String bucketName) {
        try {
            s3Client.createBucket(
                    r -> r.bucket(bucketName)
            );
            log.debug("Create Bucket successfully. bucket-name -> {}", bucketName);
        } catch (Exception e) {
            log.error("Error creating bucket: {}", e.getMessage());
            throw new RuntimeException();
        }
    }
}
