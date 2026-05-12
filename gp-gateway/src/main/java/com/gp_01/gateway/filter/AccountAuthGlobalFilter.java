package com.gp_01.gateway.filter;

import com.gp_01.common.domain.dto.LoginUserDTO;
import com.gp_01.common.enums.ResultCode;
import com.gp_01.common.exception.ForbiddenException;
import com.gp_01.common.exception.UnauthorizedException;
import com.gp_01.gateway.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.gp_01.common.constants.AuthConstants.AUTHORIZATION_HEADER;
import static com.gp_01.common.constants.AuthConstants.USER_INFO_HEADER;


@Component
@RequiredArgsConstructor
@Slf4j
public class AccountAuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        //判断是不是被过滤器排除的路径
        if (isExcludePath(path)) {
            return chain.filter(exchange);
        }
        //获取token请求头
        String token = request.getHeaders().getFirst(AUTHORIZATION_HEADER);

        LoginUserDTO userInfo;
        try {
            //TODO 解析token 获取用户信息
            userInfo = new LoginUserDTO();
            userInfo.setUserId(101L);
            //解析成功后，创建request写入请求头
            exchange.mutate().request(builder -> builder.
                    header(USER_INFO_HEADER, userInfo.getUserId().toString())
                    .build());

        } catch (Exception e) {
            throw new UnauthorizedException(e.getMessage());
        }


        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }


    public boolean isExcludePath(String path) {
        for (String pattern : authProperties.getExcludePath()) {
            boolean match = antPathMatcher.match(pattern, path);
            if (match) {
                return true;
            }
        }
        return false;
    }
}
