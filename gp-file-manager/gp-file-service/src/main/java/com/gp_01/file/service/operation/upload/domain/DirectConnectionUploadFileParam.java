package com.gp_01.file.service.operation.upload.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
@Data
@AllArgsConstructor
public class DirectConnectionUploadFileParam {

    private String bucketName;

    private String storePath;

    private String uploadId;

    private List<Integer> chunkNumbers;

    private Map<Integer, String> parts;

    private Integer expiry;

    private TimeUnit timeUnit;


//
//    public DirectConnectionUploadFileParam (String bucketName, String storePath){
//        this.bucketName = bucketName;
//        this.storePath = storePath;
//    }
//
//    public DirectConnectionUploadFileParam(String bucketName, String storePath, String uploadId, List<Integer> chunkNumbers, Integer expiry, TimeUnit timeUnit){
//        this.bucketName = bucketName;
//        this.storePath = storePath;
//        this.uploadId = uploadId;
//        this.chunkNumbers = chunkNumbers;
//        this.expiry = expiry;
//        this.timeUnit = timeUnit;
//
//    }
//
//    public DirectConnectionUploadFileParam(String bucketName, String storePath, String uploadId, Map<Integer, String> parts){
//        this.bucketName = bucketName;
//        this.storePath = storePath;
//        this.uploadId = uploadId;
//        this.parts = parts;
//    }
//
//    public DirectConnectionUploadFileParam(String bucketName, String storePath, Integer expiry, TimeUnit timeUnit){
//        this.bucketName = bucketName;
//        this.storePath = storePath;
//        this.expiry = expiry;
//        this.timeUnit = timeUnit;
//    }



}
