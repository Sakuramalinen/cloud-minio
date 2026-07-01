package com.gp_01.auth.utils;

import com.gp_01.auth.config.JWTProperties;
import com.gp_01.common.exception.BadRequestException;
import com.gp_01.common.exception.UnauthorizedException;
import com.gp_01.file.model.domain.vo.FileDetail;
import com.gp_01.model.domain.po.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.gp_01.common.constants.HttpHeaderConstants.FILE_DOWNLOAD_PATH_HEADER;

@RequiredArgsConstructor
@Component
public class JWTUtils {

    private final JWTProperties jwtProperties;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createUserToken(User user) {
        //准备token载荷数据
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());

        long now = System.currentTimeMillis();
        //计算过期时间
        Date expirationData = new Date(now + jwtProperties.getExpire() * 1000);
        //创建token
        return createToken(claims, expirationData);

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


    public String createFileToken(String downloadPath){

        Map<String, Object> claims = new HashMap<>();
        claims.put(FILE_DOWNLOAD_PATH_HEADER, downloadPath);
        long now = System.currentTimeMillis();
        Date expirationDate = new Date(now + jwtProperties.getExpire() * 1000);

        return createToken(claims, expirationDate);
    }


    private String createToken(Map<String, Object> claims, Date expirationDate){
        return Jwts.builder()
                .claims(claims) //载荷
                .expiration(expirationDate)   // 过期时间
                .signWith(key)
                .compact();
    }


}
