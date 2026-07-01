package com.gp_01.file.service.interceptor;

import com.gp_01.common.context.FileDownloadContext;
import com.gp_01.common.domain.header.FileDownloadHeaderParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

import static com.gp_01.common.constants.HttpHeaderConstants.FILE_DOWNLOAD_PATH_HEADER;

public class DownloadFileInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String header = request.getHeader(FILE_DOWNLOAD_PATH_HEADER);
        FileDownloadContext.setDownloadPath(header);

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        FileDownloadContext.removeDownloadPath();
    }
}
