package com.gp_01.file.service.operation.download;

import com.gp_01.file.service.operation.download.domain.DownloadFile;

import java.io.InputStream;

public abstract class Downloader {

    /**
     * 分片下载
     */
    public abstract InputStream downloadByChunkFile(DownloadFile downloadFile);

    /**
     * 直接下载
     */
    public abstract InputStream downloadBySingleFile(DownloadFile downloadFile);


    public InputStream download(DownloadFile downloadFile){
        if(downloadFile.getChunked()){
            return downloadByChunkFile(downloadFile);
        } else {
            return downloadBySingleFile(downloadFile);
        }
    }
}
