package com.gp_01.file.service.task;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TestHandler {

    @XxlJob("testHandler")
    public void testHandler(){
        log.debug("xxl-job hello word");

    }

}
