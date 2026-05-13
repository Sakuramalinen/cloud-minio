package com.gp_01.file;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

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

}
