package com.gp_01.file.service.operation.download;

import com.gp_01.file.service.operation.download.domian.DownloadFile;

import java.io.InputStream;

public abstract class Downloader {


    public abstract InputStream download(DownloadFile downloadFile);

    public InputStream execute(DownloadFile downloadFile){
            return download(downloadFile);
    }
}
