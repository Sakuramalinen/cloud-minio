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
     * @return [起始位置,截止位置]
     */
    public static Long[] getRequestRange(HttpServletRequest request, Long fileSize){
        try {
            String range = request.getHeader(RANGE);
            String split = range.split("=")[1];
            String[] s = split.split("-");
            long start = Long.parseLong(s[0]);

            long end = s.length > 1 ? Long.parseLong(s[1]) : fileSize - 1;
            return new Long[]{start, end};
        } catch (Exception e){
            log.error("请求头range格式异常");
            throw new BadRequestException("传输格式异常");
        }
    }

    public static void setDownloadResponse(HttpServletResponse response, String fileName, Long fileSize, String contentType){
        response.reset();
        //设置为附件下载模式， 设置下载文件名
        ContentDisposition contentDisposition = ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build();
        response.setHeader("Content-Disposition", contentDisposition.toString());
        response.setHeader("Accept-Ranges", "bytes");
        response.setContentLengthLong(fileSize);
        response.setContentType(contentType);
        response.setStatus(206);

    }

    /**
     * 设置下载响应头
     * 该方法会先清空header
     * @param response
     * @param fileName
     * @param fileSize
     * @param contentType
     */
    public static void setDownloadResponse(HttpServletResponse response, String fileName, Long fileSize, String contentType, Long offset, Long end){
        setDownloadResponse(response,fileName,fileSize,contentType);

//        response.setHeader("Content-Range", String.format("bytes %d-%d/%d", offset, offset + length, fileSize));
        response.setHeader("Content-Range", StringUtils.ContentRangeFormat(offset, end, fileSize));
        response.setContentLengthLong(end - offset + 1);
    }

    public static void setPreviewResponse(HttpServletResponse response, Long offset, Long end, Long fileSize, String contentType){
        response.reset();
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Range", StringUtils.ContentRangeFormat(offset, end, fileSize));
        response.setContentType(contentType);
        response.setContentLengthLong(end - offset + 1);
        ContentDisposition contentDisposition = ContentDisposition.inline().build();
        response.setHeader("Content-Disposition", contentDisposition.toString());

        response.setStatus(206);
    }
    public static void setPreviewResponse(HttpServletResponse response, Long fileSize){
        response.reset();
        response.setContentLengthLong(fileSize);
        response.setStatus(206);
    }



}
