package com.gp_01.file.service;

import com.gp_01.file.model.domain.dto.DownloadAuth;
import com.gp_01.file.model.domain.dto.UploadProgressSaveDTO;
import com.gp_01.file.model.domain.po.UserFile;
import com.gp_01.file.service.constants.RabbitmqFileConstants;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Formatter;
import java.util.UUID;

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
    void StringTest(){
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
    public void rabbitmqTest(){
        UploadProgressSaveDTO dto = new UploadProgressSaveDTO(2081675186204233730L, "1100000000000001000000010000000000001000010000100000000000000", 2);

        String exchange = RabbitmqFileConstants.EXCHANGE_TOPIC_FILE;
        String routingKey = RabbitmqFileConstants.RK_UPLOAD_PROGRESS_SAVE;

        rabbitTemplate.convertAndSend(exchange, routingKey, dto);
    }


}
