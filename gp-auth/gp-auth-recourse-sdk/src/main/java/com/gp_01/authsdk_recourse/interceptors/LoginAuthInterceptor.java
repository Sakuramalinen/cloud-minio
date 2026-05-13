package com.gp_01.authsdk_recourse.interceptors;

import com.gp_01.common.context.UserContext;
import com.gp_01.common.enums.ResultCode;
import com.gp_01.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new UnauthorizedException("用户未登录");
        }
        return true;
    }
}
