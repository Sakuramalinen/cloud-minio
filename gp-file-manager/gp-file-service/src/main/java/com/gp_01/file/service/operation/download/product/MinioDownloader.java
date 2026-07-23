package com.gp_01.file.service.operation.download.product;

import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.exception.CommonException;
import com.gp_01.common.exception.OSSException;
import com.gp_01.file.service.constants.MinioConstants;
import com.gp_01.file.service.operation.download.Downloader;
import com.gp_01.file.service.operation.download.domain.DownloadFile;
import com.gp_01.file.service.util.MinioUtils;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.Http;
import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
//@Component("minio_downloader")
@Component
@Slf4j
public class MinioDownloader extends Downloader {

    private final MinioClient minioClient;

    @Override
    public InputStream downloadByChunkFile(DownloadFile downloadFile) {
        GetObjectArgs args = GetObjectArgs.builder()
                .bucket(downloadFile.getBucketName())
                .object(downloadFile.getDownloadPath())
                .offset(downloadFile.getOffset())
                .length(downloadFile.getLength())
                .build();
        try {
            return minioClient.getObject(args);
        } catch (MinioException e) {
            throw new OSSException(ErrorCode.OSS_ERROR,e);
        }
    }

    @Override
    public InputStream downloadBySingleFile(DownloadFile downloadFile) {
        GetObjectArgs args = GetObjectArgs.builder()
                .bucket(downloadFile.getBucketName())
                .object(downloadFile.getDownloadPath())
                .build();
        try {
            return minioClient.getObject(args);
        } catch (MinioException e) {
            throw new OSSException(ErrorCode.OSS_ERROR,e);
        }
    }

    @Override
    public String downloadByIssuePreSignedUrl(DownloadFile downloadFile) {
        Map<String, String> directDownloadQueryParam = getDirectDownloadQueryParam(downloadFile.getContentType(), downloadFile.getFileName());
        GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                .bucket(downloadFile.getBucketName())
                .method(Http.Method.GET)
                .extraQueryParams(directDownloadQueryParam)
                .expiry(downloadFile.getExpiry(), downloadFile.getTimeUnit())
                .object(downloadFile.getDownloadPath())
                .build();
        try {
            return minioClient.getPresignedObjectUrl(args);
        } catch (MinioException e) {
            throw new OSSException(ErrorCode.OSS_ERROR,e);
        }
    }


    private Map<String, String> getDirectDownloadQueryParam(String contentType, String fileName){
        Map<String, String> queryParam = new HashMap<>();
        //设置文件MIME类型
        queryParam.put("response-content-type", contentType);
        //标识该文件为下载
        ContentDisposition contentDisposition = ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build();
        queryParam.put("response-content-disposition", contentDisposition.toString());
        //防客户端缓存
        queryParam.put("response-cache-control", "no-cache, no-store, must-revalidate");
        return queryParam;
    }


}
