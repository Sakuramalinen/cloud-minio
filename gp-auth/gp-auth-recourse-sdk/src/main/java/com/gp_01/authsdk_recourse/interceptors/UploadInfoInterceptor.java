package com.gp_01.authsdk_recourse.interceptors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gp_01.common.context.UploadInfoContext;
import com.gp_01.common.domain.context.UploadInfo;
import com.gp_01.common.enums.RequestHeaderEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.ErrorResponse;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
public class UploadInfoInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String header = request.getHeader(RequestHeaderEnum.UPLOAD_AUTHORIZATION.getCustomHeaderName());

        if(header != null){
            //解析json
            UploadInfo uploadInfo = new ObjectMapper().readValue(header, UploadInfo.class);
            //存上下文
            UploadInfoContext.setUploadInfo(uploadInfo);
        }
        return true;

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UploadInfoContext.removeUploadInfo();
    }
}
