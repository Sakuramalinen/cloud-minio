package com.gp_01.file;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;

@SpringBootTest
class GpFileApplicationTests {

    @Test
    void TimeStampTest() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            System.out.println(Instant.now().toEpochMilli());
            Thread.sleep(10);
        }
    }
    
    @Autowired
    ResourceLoader loader;
    @Test
    void filePathTest() throws URISyntaxException, IOException {
        Resource resource = loader.getResource("classpath:/static/1.txt");
        System.out.println(resource.getURI());
        System.out.println(resource.getURL());
    }

}
