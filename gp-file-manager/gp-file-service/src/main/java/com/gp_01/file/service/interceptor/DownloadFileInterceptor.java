package com.gp_01.file.service.interceptor;

import com.gp_01.common.context.FileDownloadContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.gp_01.common.constants.HttpHeaderConstants.*;

public class DownloadFileInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String storePath = request.getHeader(FILE_DOWNLOAD_PATH_HEADER);
        String userId = request.getHeader(FILE_DOWNLOAD_USERID_HEADER);

        if(storePath != null && !storePath.isEmpty() && userId != null && !userId.isEmpty()){
            FileDownloadContext.set(storePath, Long.parseLong(userId));
        }

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        FileDownloadContext.removeDownloadPath();
    }
}
