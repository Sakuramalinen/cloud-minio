package com.gp_01.file.service.operation.download.product;

import com.gp_01.file.service.constants.MinioConstants;
import com.gp_01.file.service.operation.download.Downloader;
import com.gp_01.file.service.operation.download.domain.DownloadFile;
import com.gp_01.file.service.util.MinioUtils;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@RequiredArgsConstructor
//@Component("minio_downloader")
@Component
@Slf4j
public class MinioDownloader extends Downloader {

    private final MinioClient minioClient;

    @Override
    public InputStream downloadByChunkFile(DownloadFile downloadFile) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(downloadFile.getBucketName())
                    .object(downloadFile.getDownloadPath())
                    .offset(downloadFile.getOffset())
                    .length(downloadFile.getLength())
                    .build());
        } catch (Exception e) {
            log.error("下载文件失败：下载路径：{} -> ", downloadFile.getDownloadPath(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream downloadBySingleFile(DownloadFile downloadFile) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(downloadFile.getBucketName())
                    .object(downloadFile.getDownloadPath())
                    .build());
        } catch (Exception e) {
            log.error("下载文件失败：下载路径：{} -> ",downloadFile.getDownloadPath(), e);
            throw new RuntimeException(e);
        }
    }
}
