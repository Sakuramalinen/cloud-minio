package com.gp_01.gateway.handler.impl;

import com.gp_01.auth.decrypt_sdk.utils.DecryptUtils;
import com.gp_01.common.enums.RequestHeaderEnum;
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

    private final DecryptUtils decryptUtils;

    @Override
    public RequestHeaderParseResult handle(HttpHeaders headers) {

        //获取token
        String token = headers.getFirst(RequestHeaderEnum.LOGIN_AUTHORIZATION.getRequestHeaderName());

        //不包含直接跳过
        if (token == null) {
            return new RequestHeaderParseResult(ResultEnums.SKIP);
        }
        try{
            //TODO temp
            token = token.split(" ")[1];

            //获取公钥
            String publicKey = decryptUtils.getPublicKeys().get("login");
            RSAPublicKey rsaPublicKey = decryptUtils.readPublicKey(publicKey);

            //解析token
            Claims claims = decryptUtils.JwtDecrypt(token, rsaPublicKey);
            Long userId = claims.get("userId", Long.class);

            return new RequestHeaderParseResult(RequestHeaderEnum.LOGIN_AUTHORIZATION.getCustomHeaderName(), userId);
        }catch (Exception e){
            return new RequestHeaderParseResult(ResultEnums.ERROR);
        }

    }

    @Override
    public String ErrorMessage() {
        return "登录失效";
    }


}
