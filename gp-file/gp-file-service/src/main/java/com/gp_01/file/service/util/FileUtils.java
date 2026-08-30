package com.gp_01.file.service.util;


import com.gp_01.file.service.constants.MinioConstants;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
     * @param objectPath 存储路径
     * @param fileName 文件名
     * @return contentType
     */
    public String getContentTypeByFileBinary(String objectPath, String fileName){

        byte[] buff = new byte[2048];
        try(InputStream is = minioUtils.downloadFile(objectPath)) {
            int read = is.read(buff);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return tika.detect(buff, fileName);
    }
    /**
     * 从文件二进制中获取contentType
     * @param objectPath 存储路径
     * @return contentType
     */
    public String getContentTypeByFileBinary(String objectPath){

        byte[] buff = new byte[2048];
        try(InputStream is = minioUtils.downloadFile(objectPath)) {
            int read = is.read(buff);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return tika.detect(buff);
    }

    /**
     * 分割文件名的名字和后缀
     * @param fileName 文件名
     * @return [文件名, 后缀名]
     */
    public String[] splitFileNameAndSuffix(String fileName){
        int index = fileName.lastIndexOf('.');
        if(index <= 0){
            return new String[]{fileName, ""};
        }
        String name = fileName.substring(0, index);
        String suffix = fileName.substring(index);
        return new String[]{name, suffix};
    }

    /**
     * 构建同目录下安全文件名
     * @param fileName
     * @param existFileNames 目录中其他文件名
     * @return
     */
    public String getSafeFileName(String fileName, Set<String> existFileNames){
        if(!existFileNames.contains(fileName)){
            return fileName;
        }
        String[] split = splitFileNameAndSuffix(fileName);
        String name = split[0];
        String suffix = split[1];
        int max = 0;
        Pattern pattern = Pattern.compile("^(.*)\\((\\d+)\\)(\\.[^.]+)?$");
        for (String existFileName : existFileNames) {
            Matcher matcher = pattern.matcher(existFileName);
            if(matcher.matches()){
                String base = matcher.group(1);
                int number = Integer.parseInt(matcher.group(2));
                String suf = matcher.group(3);
                suf = suf == null ? "" : suf;
                if(name.equals(base) && suffix.equals(suf)){
                    max = Math.max(number, max);
                }
            }
        }
        return name + "(" + (max + 1) + ")" + suffix;
    }

    public String getChunkAbsolutionPath(String fileMd5, Long chunkIndex){
        String basePath = getBasePath(fileMd5);
        return basePath + "/" + fileMd5 + "_" + chunkIndex + CHUNK_UPLOAD_SUFFIX;
    }

    /**
     * 获取对象存储路径
     * @param fileMd5
     * @param fileName
     * @return
     */
    public String getObjectStorePath(String fileMd5, String fileName){
        String basePath = getBasePath(fileMd5);
        String fileExtendName = getFileExtendName(fileName);
        return MinioConstants.ORIGINAL_PATH_HEAD + "/" + basePath+ "/" + fileMd5 + fileExtendName;
    }

    /**
     * 获取缩略图存储路径
     * @param fileMd5
     * @param extendName
     * @return
     */
    public String getThumbnailFileStorePath(String fileMd5, String extendName){
        String basePath = getBasePath(fileMd5);
        return MinioConstants.THUMBNAIL_PATH_HEAD + "/" + basePath+ "/" + fileMd5 + extendName;
    }

    /**
     * 获取头像存储路径
     * @param filename
     * @return
     */
    public String getAvatarFileStorePath(String filename, Long userId){
        return MinioConstants.AVATAR_PATH_HEAD + "/" +userId + "/" + filename;
    }

    public String getBasePath(String fileMd5){
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1);
    }

}
