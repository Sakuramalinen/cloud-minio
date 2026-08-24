package com.gp_01.file.service.operation.download;

import com.gp_01.file.service.operation.download.domain.DownloadFile;
import io.minio.errors.*;
import org.springframework.http.ContentDisposition;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public abstract class Downloader {

    /**
     * 分片下载
     */
    public abstract InputStream downloadByChunkFile(DownloadFile downloadFile);

    /**
     * 直接下载
     */
    public abstract InputStream downloadBySingleFile(DownloadFile downloadFile);

    /*
     * 直连OSS下载
     */
    public abstract String downloadByIssuePreSignedUrl(DownloadFile downloadFile);


    public InputStream download(DownloadFile downloadFile){
        if(downloadFile.getChunked()){
            return downloadByChunkFile(downloadFile);
        } else {
            return downloadBySingleFile(downloadFile);
        }
    }



}
