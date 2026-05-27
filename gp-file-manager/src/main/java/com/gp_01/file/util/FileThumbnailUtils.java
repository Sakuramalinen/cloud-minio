package com.gp_01.file.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.xmlunit.builder.Input;

import java.io.*;
@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "gp.thumbnails")
public class FileThumbnailUtils {

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
        }
        return null;
    }
}
