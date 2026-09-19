package com.gp_01.file.service.oss.delete.product;

import com.gp_01.file.service.oss.OSS;
import com.gp_01.file.service.oss.delete.Deleter;
import io.minio.MinioClient;
import io.minio.RemoveObjectsArgs;
import io.minio.messages.DeleteRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MinioDeleter implements Deleter {

    private final MinioClient minioClient;


    private final OSS oss;



    @Override
    public void deleteObjects(List<String> objectPaths) {

        ArrayList<DeleteRequest.Object> list = new ArrayList<>();
        for (String object : objectPaths) {
            DeleteRequest.Object obj = new DeleteRequest.Object(object);
            list.add(obj);
        }

        RemoveObjectsArgs args = RemoveObjectsArgs.builder()
                .bucket(oss.getBucketName())
                .objects(list)
                .build();

        minioClient.removeObjects(args);


    }
}
