package com.gp_01.authsdk_gateway.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPublicKey;


@Component
@Slf4j
public class AuthUtil {

    public Claims parseToken(String token, RSAPublicKey publicKey){
        return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
    }

}
