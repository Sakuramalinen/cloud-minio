package com.gp_01.authsdk_recourse.interceptors;

import com.gp_01.common.constants.AuthConstants;
import com.gp_01.common.context.UserContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;



@Slf4j
public class UserInfoInterceptor implements HandlerInterceptor {


    /**
     * 获取请求头用户信息
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String authHeader = request.getHeader(AuthConstants.USER_INFO_HEADER);
        if (authHeader == null) {
            return true;
        }
        try {
            //存入ThreadLocal
            long userId = Long.parseLong(authHeader);
            UserContext.setUser(userId);
        } catch (NumberFormatException e) {
            log.error("用户信息格式不正确,{} :", authHeader, e);
        }
        return true;
    }


    /**
     * 删除ThreadLocal用户信息
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.removeUser();
    }
}
