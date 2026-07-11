package com.gp_01.authsdk_gateway.utils;

import com.gp_01.authsdk_gateway.config.JWTProperties;
import com.gp_01.common.domain.dto.FileDownloadDTO;
import com.gp_01.common.domain.dto.LoginUserDTO;
import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.exception.ForbiddenException;
import com.gp_01.common.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static com.gp_01.common.constants.HttpHeaderConstants.*;

@RequiredArgsConstructor
@Component
@Slf4j
public class AuthUtil {

    private final JWTProperties jwtProperties;

    private SecretKey key;

    @PostConstruct
    private void init() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public LoginUserDTO parseUserToken(String token) {
        Claims payload = null;
        try {
            payload = parseToken(token);
        } catch (Exception e) {
            log.debug("用户未登录");
            throw new UnauthorizedException(ErrorCode.LOGIN_ERROR);
        }
        if(payload.getExpiration().before(new Date())){
            log.debug("用户登录过期");
            throw new UnauthorizedException(ErrorCode.LOGIN_EXPIRATION_ERROR);
        }
        Long userId = payload.get("userId",Long.class);

        return new LoginUserDTO(userId);
    }


    public Claims parseToken(String token){
        return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
    }

    public FileDownloadDTO parseFileToken(String downloadFileToken){
        Claims payload = null;
        try{
            payload = parseToken(downloadFileToken);
        } catch (Exception e){
            log.debug("解析下载token失败");
            throw new ForbiddenException(ErrorCode.AUTHORITY_ERROR.getCode(), "校验下载凭证失败");
        }
        if(payload.getExpiration().before(new Date())){
            log.debug("下载token过期");
            throw new ForbiddenException(ErrorCode.AUTHORITY_EXPIRATION_ERROR.getCode(), "下载凭证过期");
        }
        String storePath = payload.get(FILE_DOWNLOAD_PATH_HEADER, String.class);
        Long userId = payload.get(FILE_DOWNLOAD_USERID_HEADER, Long.class);
        return new FileDownloadDTO(storePath, userId);
    }
}
