package com.gp_01.file.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Data
@Component
@ConfigurationProperties(prefix = "gp.file-service")
public class FileServiceProperties {
    /**
     * 回收站保存时间
     */
    private Long recycleSaveDay = 14L;

}
