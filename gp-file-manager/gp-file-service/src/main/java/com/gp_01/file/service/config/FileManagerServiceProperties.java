package com.gp_01.file.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Data
@Component
@ConfigurationProperties(prefix = "gp.file-manager")
public class FileManagerServiceProperties {
    /**
     * 回收站保存时间
     */
    private Integer recycleSaveDay = 10;

    private String localStoragePath;

    private String rootPath;

//    private String storeType;
}
