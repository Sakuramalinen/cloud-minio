package com.gp_01.auth.encrypt_sdk.utils;

import io.jsonwebtoken.Jwts;
import lombok.Data;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@ConfigurationProperties(prefix = "gp.auth.token")
@Data
public class EncryptUtils {
    private Map<String, String> privateKeys;


    public  String JwtEncrypt(Map<String, Object> claims, PrivateKey privateKey, Long expire, TimeUnit timeUnit) {

        //计算过期时间
        long millis = timeUnit.toMillis(expire);
        Date expireTime = new Date(System.currentTimeMillis() + millis);

        //创建token
        return Jwts.builder()
                .claims(claims) //载荷
                .expiration(expireTime)   // 过期时间
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public  RSAPrivateKey readPrivateKey(String privateKey) {

        byte[] der = Base64.getDecoder().decode(privateKey);
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(der));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
