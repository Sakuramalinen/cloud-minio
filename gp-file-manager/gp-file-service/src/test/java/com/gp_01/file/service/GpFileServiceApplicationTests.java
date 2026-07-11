package com.gp_01.file.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GpFileServiceApplicationTests {

    @Test
    void contextLoads() {

        String s = "123/";
        String[] split = s.split("/");
        System.out.println(split[0]);
        System.out.println(split.length);
    }

}
