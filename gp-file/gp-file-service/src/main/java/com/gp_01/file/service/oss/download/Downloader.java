package com.gp_01.file.service.oss.download;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public interface Downloader {


    //获取下载输入流
    InputStream getDownloadInputStream(String bucketName, String objectPath);

    //获取下载预签名
    String downloadPreSign(String bucketName, String fileName, String objectPath, String contentType, Integer expiry, TimeUnit timeUnit);



}
