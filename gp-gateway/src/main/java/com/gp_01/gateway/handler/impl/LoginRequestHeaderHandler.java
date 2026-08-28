package com.gp_01.gateway.handler.impl;

import com.gp_01.authsdk_gateway.utils.AuthUtil;
import com.gp_01.common.enums.RequestHeaderEnum;
import com.gp_01.common.utils.RSAUtils;
import com.gp_01.gateway.config.PublicKeyProperties;
import com.gp_01.gateway.domain.RequestHeaderParseResult;
import com.gp_01.gateway.domain.ResultEnums;
import com.gp_01.gateway.handler.RequestHeaderHandler;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class LoginRequestHeaderHandler implements RequestHeaderHandler {

    private final AuthUtil authUtil;

    private final PublicKeyProperties publicKeyProperties;


    @Override
    public RequestHeaderParseResult handle(HttpHeaders headers) {



        //获取token
        String token = headers.getFirst(RequestHeaderEnum.LOGIN_AUTHORIZATION.getRequestHeaderName());
        RequestHeaderParseResult result = new RequestHeaderParseResult();

        //不包含直接跳过
        if (token == null) {
            result.setResultEnums(ResultEnums.SKIP);
            return result;
        }

        //TODO temp
        token = token.split(" ")[1];

        RSAPublicKey rsaPublicKey = RSAUtils.readPublicKey(publicKeyProperties.getLoginPublicKey());

        //解析token
        Claims claims = authUtil.parseToken(token, rsaPublicKey);

        long userId = Long.parseLong(claims.get("userId").toString());
        HashMap<String, String> map = new HashMap<>();
        map.put(RequestHeaderEnum.LOGIN_AUTHORIZATION.getCustomHeaderName(), String.valueOf(userId));
        result.setHeaders(map);
        result.setResultEnums(ResultEnums.SUCCESS);
        return result;
    }

    @Override
    public String ErrorMessage() {
        return "登录失效";
    }


}
