package com.gp_01.auth.decrypt_sdk.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "gp.auth.token")
@Data
public class DecryptUtils {

    private Map<String, String> publicKeys;


    public Claims JwtDecrypt(String token, PublicKey publicKey){
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public  RSAPublicKey readPublicKey(String publicKey) {
        try {
            byte[] der = Base64.getDecoder().decode(publicKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(der));
        } catch (Exception e) {
            throw new IllegalStateException("解析公钥失败: " + publicKey, e);
        }
    }




}
