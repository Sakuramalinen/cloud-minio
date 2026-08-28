package com.gp_01.auth.service.utils;

import com.gp_01.auth.service.config.JWTProperties;
import com.gp_01.common.enums.ErrorCode;
import com.gp_01.common.exception.BadRequestException;
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
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.Date;
import java.util.Map;

@RequiredArgsConstructor
@Component
@Slf4j
public class JWTUtils {


    private final RSAPrivateKey rsaPrivateKey;


    public String createToken(Map<String, Object> claims, Long expire) {

        //计算过期时间
        long now = System.currentTimeMillis();
        Date expirationDate = new Date(now + expire * 1000);
        //创建token
        return Jwts.builder()
                .claims(claims) //载荷
                .expiration(expirationDate)   // 过期时间
                .signWith(rsaPrivateKey, Jwts.SIG.RS256)
                .compact();
    }

    public Claims parseToken(String token, PublicKey publicKey) {
        Claims payload = null;
        try {
            payload = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new UnauthorizedException(ErrorCode.LOGIN_ERROR);
        }
        if(payload.getExpiration().before(new Date())){
            throw new BadRequestException(ErrorCode.LOGIN_EXPIRATION_ERROR);
        }
        return payload;
    }



}
