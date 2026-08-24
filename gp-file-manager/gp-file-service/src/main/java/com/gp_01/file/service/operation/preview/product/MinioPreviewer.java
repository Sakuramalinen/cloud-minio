package com.gp_01.file.service.operation.preview.product;

import com.gp_01.file.service.operation.preview.Previewer;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.Http;
import io.minio.MinioAsyncClient;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
@Component
public class MinioPreviewer extends Previewer {

//    private final MinioAsyncClient minioAsyncClient;

    private final MinioClient minioClient;

    public String getPreviewPreSignedUrl(String bucket, String objectPath, String contentType, Integer expiry, TimeUnit unit){
        Map<String, String> queryParamsMap = getQueryParamsMap(contentType);

        GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                .bucket(bucket)
                .object(objectPath)
                .method(Http.Method.GET)
                .expiry(expiry, unit)
                .extraQueryParams(queryParamsMap)
                .build();
        try {
            return minioClient.getPresignedObjectUrl(args);
        } catch (MinioException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> getQueryParamsMap(String contentType){
        Map<String, String> queryParamsMap = new HashMap<>();
        queryParamsMap.put("response-content-type", contentType);
        String contentDisposition = ContentDisposition.inline().build().toString();
        queryParamsMap.put("response-content-disposition", contentDisposition);
        return queryParamsMap;
    }
}
