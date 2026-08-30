package com.gp_01.file.service.util;

import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.exception.CommonException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.*;
@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "gp.file.thumbnails")
public class ThumbnailUtils {

    private int width;

    /**
     * 创建缩略图输入流
     * @param is
     * @return
     * @throws IOException
     */
    public byte[] createThumbnailBytes(InputStream is){

        try (ByteArrayOutputStream os = new ByteArrayOutputStream();){
            Thumbnails.of(is)
                    .width(width)
                    .keepAspectRatio(true)
                    .outputQuality(0.8f)
                    .toOutputStream(os);
            return os.toByteArray();
        } catch (Exception e){
            log.error("制作缩略图失败：",e);
            throw new CommonException(ErrorCode.SERVICE_ERROR.getCode(), "制作缩略图失败");
        }
    }
}
