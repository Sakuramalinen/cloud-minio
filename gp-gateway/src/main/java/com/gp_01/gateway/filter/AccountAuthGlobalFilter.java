package com.gp_01.gateway.filter;

import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.exception.BadRequestException;
import com.gp_01.gateway.domain.ResultEnums;
import com.gp_01.gateway.domain.RequestHeaderParseResult;
import com.gp_01.gateway.handler.RequestHeaderHandleRegister;
import com.gp_01.gateway.handler.RequestHeaderHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;



@Component
@RequiredArgsConstructor
@Slf4j
public class AccountAuthGlobalFilter implements GlobalFilter, Ordered {

    private final RequestHeaderHandleRegister requestHeaderHandleRegister;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        //复制一个新的request写入请求
        ServerHttpRequest.Builder builder = exchange.getRequest().mutate();

        //处理请求头
        for (RequestHeaderHandler handler : requestHeaderHandleRegister.getHandlers()) {
            RequestHeaderParseResult res = handler.handle(headers);

            ResultEnums resultEnums = res.getResultEnums();

            //解析成功
            if(resultEnums.equals(ResultEnums.SUCCESS)){
                builder.header(res.getHeaderKey(), res.getHeaderValue());
            }
            //解析失败
            if(resultEnums.equals(ResultEnums.ERROR)){
                throw new BadRequestException(ErrorCode.AUTHORITY_ERROR.getCode(), handler.ErrorMessage());
            }
        }

        ServerWebExchange webExchange = exchange.mutate().request(builder.build()).build();
        return chain.filter(webExchange);
    }

    @Override
    public int getOrder() {
        return 1;
    }



}
