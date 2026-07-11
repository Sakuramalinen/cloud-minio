package com.gp_01.file.service.util;


import com.gp_01.file.service.config.FileServiceProperties;
import com.gp_01.file.service.constants.MinioConstants;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;


@RequiredArgsConstructor
@Data
@Component
public class FileUtils {

    private final Tika tika;

    private final MinioUtils minioUtils;

    private static final String CHUNK_UPLOAD_SUFFIX = ".chunkUploading";


    public  String getFileExtendName(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    /**
     * 从文件二进制中获取contentType
     * @param originalPath 存储路径
     * @param fileName 文件名
     * @return contentType
     */
    public String getFileType(String originalPath, String fileName){

        byte[] buff = new byte[2048];
        try(InputStream is = minioUtils.downloadFile(originalPath)) {
            int read = is.read(buff);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tika.detect(buff, fileName);
    }

    public String getChunkAbsolutionPath(String fileMd5, Long chunkIndex){
        String basePath = getBasePath(fileMd5);
        return basePath + "/" + fileMd5 + "_" + chunkIndex + CHUNK_UPLOAD_SUFFIX;
    }

    public String getOriginalFileStorePath(String fileMd5, String extendName){
        String basePath = getBasePath(fileMd5);
        return MinioConstants.ORIGINAL_PATH_HEAD + "/" + basePath+ "/" + fileMd5 + extendName;
    }

    public String getThumbnailFileStorePath(String fileMd5, String extendName){
        String basePath = getBasePath(fileMd5);
        return MinioConstants.THUMBNAIL_PATH_HEAD + "/" + basePath+ "/" + fileMd5 + extendName;
    }

    public String getBasePath(String fileMd5){
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1);
    }

}
