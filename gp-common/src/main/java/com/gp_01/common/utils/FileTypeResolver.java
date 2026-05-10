package com.gp_01.common.utils;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.gp_01.common.enums.FileTypeEnum;
import org.springframework.stereotype.Component;

@Component
public class FileTypeResolver {

    private FileTypeResolver() {

    }

    public static FileTypeEnum parse(String contentType) {
        if (contentType == null || StringUtils.isEmpty(contentType)) {
            return FileTypeEnum.OTHER;
        }
        int index = contentType.indexOf("/");
        if(index == -1){
            return FileTypeEnum.OTHER;
        }
        String type = contentType.substring(0, index);
        return switch (type) {
            case "image" -> FileTypeEnum.IMAGE;
            case "audio" -> FileTypeEnum.AUDIO;
            case "text", "application" -> FileTypeEnum.TEXT;
            case "video" -> FileTypeEnum.VIDEO;
            default -> FileTypeEnum.OTHER;
        };

    }


}
