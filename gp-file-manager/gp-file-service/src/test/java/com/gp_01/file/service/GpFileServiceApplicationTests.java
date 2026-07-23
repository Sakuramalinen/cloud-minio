package com.gp_01.file.service;

import org.junit.jupiter.api.Test;
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




}
