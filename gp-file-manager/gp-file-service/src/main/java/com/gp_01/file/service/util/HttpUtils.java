package com.gp_01.file.service.util;

import com.gp_01.common.exception.BadRequestException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class HttpUtils {

    private static final String RANGE = "Range";

    /**
     * 获取请求下载范围
     * @param request request
     * @return [起始位置,下载长度]
     */
    public static Long[] getRange(HttpServletRequest request){
        try {
            String range = request.getHeader(RANGE);
            String split = range.split("=")[1];

            long start = Long.parseLong(split.split("-")[0]);
            long end = Long.parseLong(split.split("-")[1]);
            long length = end - start + 1;
            return new Long[]{start, length};
        } catch (Exception e){
            log.error("请求头range格式异常");
            throw new BadRequestException("传输格式异常");
        }
    }

    /**
     * 设置下载请求头
     * 该方法会清空header
     * @param response
     * @param fileName
     * @param fileSize
     * @param contentType
     */
    public static void setDownloadHeaders(HttpServletResponse response, String fileName, Long fileSize, String contentType, Long offset, Long length){
        response.reset();
        //设置为附件下载模式， 设置下载文件名
        ContentDisposition contentDisposition = ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build();
        response.setHeader("Content-Disposition", contentDisposition.toString());
        response.setHeader("Accept-Ranges", "bytes");
        response.setContentLengthLong(fileSize);
        response.setContentType(contentType);

        response.setHeader("Content-Range", String.format("bytes %d-%d/%d", offset, offset + length, fileSize));
        response.setContentLengthLong(length);
    }

    public static void setDownloadHeaders(HttpServletResponse response, String fileName, Long fileSize, String contentType){
        response.reset();
        //设置为附件下载模式， 设置下载文件名
        ContentDisposition contentDisposition = ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build();
        response.setHeader("Content-Disposition", contentDisposition.toString());
        response.setHeader("Accept-Ranges", "bytes");
        response.setContentLengthLong(fileSize);
        response.setContentType(contentType);

    }

}
