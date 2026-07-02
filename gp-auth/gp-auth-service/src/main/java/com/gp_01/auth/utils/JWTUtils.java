package com.gp_01.auth.utils;

import com.gp_01.auth.config.JWTProperties;
import com.gp_01.common.exception.BadRequestException;
import com.gp_01.common.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class JWTUtils {

    private final JWTProperties jwtProperties;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(Map<String, Object> claims, Long expire) {

        //计算过期时间
        long now = System.currentTimeMillis();
        Date expirationDate = new Date(now + expire * 1000);

        //创建token
        return Jwts.builder()
                .claims(claims) //载荷
                .expiration(expirationDate)   // 过期时间
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        Claims payload = null;
        try {
            payload = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new UnauthorizedException("登录失败");
        }
        if(payload.getExpiration().before(new Date())){
            throw new BadRequestException("登录过期");
        }
        return payload;
    }



}
