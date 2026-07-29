package com.gp_01.file.service.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.util.Map;
import java.util.TreeMap;

@Data
@Component
@ConfigurationProperties(prefix = "gp.file-service")
public class FileServiceProperties {
    /**
     * 回收站保存时间
     * 默认保存14天
     */
    private Long recycleSaveDay = 14L;

    /**
     * 分片上传阈值，文件大小超过阈值 开启分片上传
     * 默认100MB开启分片
     */
    private DataSize chunkUploadThreshold = DataSize.ofMegabytes(50);

    /**
     * 分片策略
     * 例如 1GB: 50MB 表示1GB以上的文件的分片大小为50MB
     */
    private Map<DataSize, DataSize> chunkStrategyMap;



}

