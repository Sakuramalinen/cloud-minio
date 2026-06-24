package com.gp_01.authsdk_gateway.utils;

import com.gp_01.authsdk_gateway.config.JWTProperties;
import com.gp_01.common.domain.dto.LoginUserDTO;
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
@RequiredArgsConstructor
@Component
public class AuthUtil {

    private final JWTProperties jwtProperties;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public LoginUserDTO parse(String token) {
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
        Long userId = payload.get("userId",Long.class);

        return new LoginUserDTO(userId);
    }
}
