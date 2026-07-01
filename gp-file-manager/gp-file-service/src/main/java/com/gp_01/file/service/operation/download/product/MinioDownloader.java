package com.gp_01.file.service.operation.download.product;

import com.gp_01.file.service.operation.download.Downloader;
import com.gp_01.file.service.operation.download.domian.DownloadFile;
import com.gp_01.file.service.util.MinioUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@RequiredArgsConstructor
//@Component("minio_downloader")
@Component
public class MinioDownloader extends Downloader {

    private final MinioUtils minioUtils;

    @Override
    public InputStream download(DownloadFile downloadFile) {
        if(downloadFile.getChunked()){
            return minioUtils.downloadFile(downloadFile.getIntegratePath(), downloadFile.getOffset(), downloadFile.getLength());
        } else {
            return minioUtils.downloadFile(downloadFile.getIntegratePath());
        }
    }
}
