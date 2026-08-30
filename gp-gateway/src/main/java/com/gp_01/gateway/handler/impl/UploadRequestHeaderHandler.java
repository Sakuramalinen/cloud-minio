package com.gp_01.gateway.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gp_01.auth.decrypt_sdk.utils.DecryptUtils;
import com.gp_01.common.domain.context.UploadInfo;
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
public class UploadRequestHeaderHandler implements RequestHeaderHandler {

    private final DecryptUtils decryptUtils;

    @Override
    public RequestHeaderParseResult handle(HttpHeaders headers) {

        String token = headers.getFirst(RequestHeaderEnum.UPLOAD_AUTHORIZATION.getRequestHeaderName());
        if (token == null) {
            return new RequestHeaderParseResult(ResultEnums.SKIP);
        }

        try{
            //加载公钥
            String publicKey = decryptUtils.getPublicKeys().get("upload");
            RSAPublicKey rsaPublicKey = decryptUtils.readPublicKey(publicKey);

            //解析token
            Claims claims = decryptUtils.JwtDecrypt(token, rsaPublicKey);
            String jsonString = claims.get(RequestHeaderEnum.UPLOAD_AUTHORIZATION.getCustomHeaderName(), String.class);

            UploadInfo uploadInfo = new ObjectMapper().readValue(jsonString, UploadInfo.class);


            return new RequestHeaderParseResult(RequestHeaderEnum.UPLOAD_AUTHORIZATION.getCustomHeaderName(), uploadInfo);
        }catch (Exception e){
            return new RequestHeaderParseResult(ResultEnums.ERROR);
        }


    }

    @Override
    public String ErrorMessage() {
        return "上传信息不存在";
    }
}
