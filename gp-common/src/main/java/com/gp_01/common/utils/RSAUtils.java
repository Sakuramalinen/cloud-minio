package com.gp_01.common.utils;

import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAUtils {


    public static String readPem(String classpathPath){
        try (InputStream in = new ClassPathResource(classpathPath).getInputStream()) {
            String pem = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return pem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
        } catch (Exception e) {
            throw new IllegalStateException("读取密钥失败: " + classpathPath, e);
        }
    }


    public static RSAPrivateKey readPrivateKey(String privateKey) {

        byte[] der = Base64.getDecoder().decode(privateKey);
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(der));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static RSAPublicKey readPublicKey(String publicKey) {
        try {
            byte[] der = Base64.getDecoder().decode(publicKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(der));
        } catch (Exception e) {
            throw new IllegalStateException("解析公钥失败: " + publicKey, e);
        }
    }
}
