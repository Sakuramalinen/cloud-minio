package com.gp_01.file.service.oss.preview.product;

import com.gp_01.file.service.oss.preview.Previewer;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.Http;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
@Component
public class MinioPreviewer implements Previewer {

//    private final MinioAsyncClient minioAsyncClient;

    private final MinioClient minioClient;

    @Override
    public String previewPreSignUrl(String bucket, String objectPath, String contentType, Integer expiry, TimeUnit unit){
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
