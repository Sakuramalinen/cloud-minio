package com.gp_01.file.service.oss;

public interface BucketManipulator {

    void createBucketIfNotExist(String bucketName);
}
