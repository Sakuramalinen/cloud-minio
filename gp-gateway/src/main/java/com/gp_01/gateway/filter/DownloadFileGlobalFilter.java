package com.gp_01.gateway.filter;

import com.gp_01.authsdk_gateway.utils.AuthUtil;
import com.gp_01.common.domain.dto.FileDownloadDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.gp_01.common.constants.HttpHeaderConstants.*;

@Component
@RequiredArgsConstructor
public class DownloadFileGlobalFilter implements GlobalFilter, Ordered {

    private final AuthUtil authUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerWebExchange res = null;
        ServerHttpRequest request = exchange.getRequest();
        //获取token
        String downloadFileHeader = request.getHeaders().getFirst(FILE_DOWNLOAD_INFO_TOKEN);
        if (downloadFileHeader != null) {
            //解析token
            FileDownloadDTO fileDownloadDTO = authUtil.parseFileToken(downloadFileHeader);
            //克隆request添加header
            res = exchange.mutate().request(builder -> builder
                            .header(FILE_DOWNLOAD_PATH_HEADER, fileDownloadDTO.getStorePath())
                            .header(FILE_DOWNLOAD_USERID_HEADER, String.valueOf(fileDownloadDTO.getUserId()))
            ).build();
        }
        if (res != null) {
            return chain.filter(res);
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
