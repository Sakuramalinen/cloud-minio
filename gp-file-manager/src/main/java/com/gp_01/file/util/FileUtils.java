package com.gp_01.file.util;


import com.gp_01.common.enums.FileTypeEnum;
import com.gp_01.file.config.FileManagerServiceProperties;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.gp_01.common.enums.FileTypeEnum.*;

@RequiredArgsConstructor
@Data
@Component
public class FileUtils {
    public static String LOCAL_STORAGE_PATH;
    public static String ROOT_PATH;

    private final FileManagerServiceProperties properties;

    public static final String[] IMG_FILE = {"bmp", "jpg", "png", "tif", "gif", "jpeg"};
    public static final String[] DOC_FILE = {"doc", "docx", "ppt", "pptx", "xls", "xlsx", "txt", "hlp", "wps", "rtf", "html", "pdf"};
    public static final String[] VIDEO_FILE = {"avi", "mp4", "mpg", "mov", "swf"};
    public static final String[] MUSIC_FILE = {"wav", "aif", "au", "mp3", "ram", "wma", "mmf", "amr", "aac", "flac"};
    public static final String[] TXT_FILE = {"txt", "html", "java", "xml", "js", "css", "json", "sql"};

    private final Tika tika = new Tika();
    private final MinioUtils minioUtils;

    @PostConstruct
    public void init() {
        LOCAL_STORAGE_PATH = properties.getLocalStoragePath();
        ROOT_PATH = properties.getRootPath();
    }

    public  String getStaticPath() {
        if (LOCAL_STORAGE_PATH != null && !LOCAL_STORAGE_PATH.isEmpty()) {
            return Paths.get(LOCAL_STORAGE_PATH).toAbsolutePath().normalize().toString();
        }
        return Paths.get(getObjectAbsolutePath(), "static").toAbsolutePath().normalize().toString();

    }

    /**
     * 获取当前项目绝对路径
     *
     * @return
     */
    public  String getObjectAbsolutePath() {
        return System.getProperty("user.dir");
    }

    public  String getFileExtendName(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    public  FileTypeEnum getFileType(String integratePath, String fileName){

        byte[] buff = new byte[2048];
        try(InputStream is = minioUtils.downloadFile(integratePath)) {
            int read = is.read(buff);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String mime = tika.detect(buff, fileName);
        if (mime.startsWith("video/")) return VIDEO;
        if (mime.startsWith("audio/")) return AUDIO;
        if (mime.startsWith("image/")) return IMAGE;
        if(mime.startsWith("text/")) return TEXT;
        else {
            return OTHER;
        }
    }
}
