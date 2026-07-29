package com.gp_01.file.service.constants;

import com.gp_01.file.service.util.StringFormatter;
import com.gp_01.file.service.util.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Formatter;

@Component
public class RedisKeyFormatter {

    private static final String CHUNK_UPLOAD_INFO_FORMAT ="gb_01:file-service:upload-file:%d:%d";
    private static final String CHUNK_UPLOAD_PROGRESS_INFO_FORMAT = "gb_01:file-service:upload-progress:%d:%d";

    /**
     * 获取文件上传信息缓存key
     * @param userId
     * @param taskId
     * @return
     */
    public static String fileUploadInfoKey(Long userId, Long taskId){
        return String.format(CHUNK_UPLOAD_INFO_FORMAT, userId, taskId);
    }

    /**
     * 获取分片上传进度信息缓存key
     * @param userId
     * @param taskId
     * @return
     */
    public static String chunkUploadProgressInfoKey(Long userId, Long taskId){
        return String.format(CHUNK_UPLOAD_PROGRESS_INFO_FORMAT, userId, taskId);
    }

}
