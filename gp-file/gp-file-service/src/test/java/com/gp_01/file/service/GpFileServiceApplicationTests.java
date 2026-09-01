package com.gp_01.file.service;

import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.exception.CommonException;
import com.gp_01.file.model.domain.dto.DownloadAuth;
import com.gp_01.file.model.domain.dto.UploadProgressSaveDTO;
import com.gp_01.file.model.domain.po.UploadTaskRecord;
import com.gp_01.file.model.domain.po.UserFile;
import com.gp_01.file.service.constants.RabbitmqFileConstants;
import com.gp_01.file.service.mapper.UploadTaskRecordMapper;
import com.gp_01.file.service.util.MinioUtils;
import io.minio.ListPartsArgs;
import io.minio.MinioAsyncClient;
import io.minio.ObjectArgs;
import io.minio.messages.ListPartsResult;
import io.minio.messages.Part;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@SpringBootTest
class GpFileServiceApplicationTests {

    @Test
    void contextLoads() {

        String s = "123/";
        String[] split = s.split("/");
        System.out.println(split[0]);
        System.out.println(split.length);
    }

    @Test
    void StringTest() {
        Long userId = 101L;
        String ticket = UUID.randomUUID().toString();
        Formatter format = new Formatter().format("gp_01:file:download:ticket:%d:%s", userId, ticket);
        System.out.println(format.toString());
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

//    @Test
//    public void rabbitTest(){
//        String queueName = "simple.queue";
//        String message = "hello,simple queue";
//        DownloadAuth downloadAuth = new DownloadAuth();
//        downloadAuth.setFileName("filename");
//        downloadAuth.setDownloadPath("downloadPath");
//        downloadAuth.setContentType("image/png");
//
//        rabbitTemplate.convertAndSend(queueName, downloadAuth);
//    }

    @Test
    public void rabbitmqTest() {
        UploadProgressSaveDTO dto = new UploadProgressSaveDTO(2081675186204233730L, "1100000000000001000000010000000000001000010000100000000000000", 2);

        String exchange = RabbitmqFileConstants.EXCHANGE_TOPIC_FILE;
        String routingKey = RabbitmqFileConstants.RK_UPLOAD_PROGRESS_SAVE;

        rabbitTemplate.convertAndSend(exchange, routingKey, dto);
    }

    @Autowired
    private MinioAsyncClient minioAsyncClient;
    @Autowired
    private UploadTaskRecordMapper uploadTaskRecordMapper;

    @Test
    public void getChunk() throws NoSuchFieldException, IllegalAccessException, ExecutionException, InterruptedException {

        UploadTaskRecord uploadTaskRecord = uploadTaskRecordMapper.selectById(2085585142431481857L);


        String bucketName = uploadTaskRecord.getBucketName();
        String uploadId = uploadTaskRecord.getUploadId();
        String objectPath = uploadTaskRecord.getObjectPath();

        List<Part> parts = new ArrayList<>();
        Integer partNumberMarker = null;
        do {
            //获取所有分片
            ListPartsArgs partsArgs = ListPartsArgs.builder()
                    .bucket(bucketName)
                    .maxParts(100)
                    .uploadId(uploadId)
                    .partNumberMarker(partNumberMarker)
                    .build();
            //TODO 通过反射设置字段
            Field objectName = ObjectArgs.class.getDeclaredField("objectName");
            objectName.setAccessible(true);
            objectName.set(partsArgs, objectPath);
            ListPartsResult result = minioAsyncClient.listParts(partsArgs).get().result();
            parts.addAll(result.parts());
            partNumberMarker = result.nextPartNumberMarker();
        } while (partNumberMarker != null && partNumberMarker != 0);


        System.out.println("123");

    }

    @Test
    public void substringTest(){
        String fileName = "123.txt";
        String originalFileName = fileName.substring(0, fileName.lastIndexOf("."));

        System.out.println(originalFileName);

    }

}
