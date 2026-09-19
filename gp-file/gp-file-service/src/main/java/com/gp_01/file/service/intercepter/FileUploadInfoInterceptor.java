package com.gp_01.file.service.intercepter;

import com.gp_01.common.context.UploadInfoContext;
import com.gp_01.common.context.UserContext;
import com.gp_01.common.domain.context.UploadInfo;
import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.enums.RequestHeaderEnum;
import com.gp_01.common.exception.CommonException;
import com.gp_01.file.service.constants.RedisKeyFormatter;
import com.gp_01.file.service.util.RedisUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class FileUploadInfoInterceptor implements HandlerInterceptor {

    private final RedisUtils redisUtils;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String header = request.getHeader(RequestHeaderEnum.UPLOAD_AUTHORIZATION.getRequestHeaderName());
        if(header == null){
            return true;
        }

        Long userId = UserContext.getUser();
        String key = RedisKeyFormatter.fileUploadInfoKey(userId, header);
        UploadInfo uploadInfo = redisUtils.getObjectAndReNew(key, UploadInfo.class, 1L, TimeUnit.DAYS);

        if(uploadInfo == null){
            throw new CommonException(ErrorCode.AUTHORITY_EXPIRATION_ERROR);
        }
        UploadInfoContext.setUploadInfo(uploadInfo);



        return true;
//        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        UploadInfoContext.removeUploadInfo();

        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
