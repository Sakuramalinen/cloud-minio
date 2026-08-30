package com.gp_01.gateway;

import com.gp_01.auth.decrypt_sdk.utils.DecryptUtils;
import com.gp_01.common.domain.context.UploadInfo;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.beans.Encoder;
import java.security.interfaces.RSAPublicKey;

@SpringBootTest
class GpGatewayApplicationTests {

    @Test
    void contextLoads() {
    }


    @Autowired
    private DecryptUtils decryptUtils;

    @Test
    void testJWT(){
//        String privateKey = "";
        String token =

        "eyJhbGciOiJSUzI1NiJ9.eyJ1cGxvYWQtaW5mbyI6IntcInVwbG9hZElkXCI6bnVsbCxcIm9iamVjdFBhdGhcIjpcIm9yaWdpbmFsLzMvNi8zNjIyZjQ0MzA0NGViNWQ5NzhiMjdmYTA3NjhjNDBhMS5qcGdcIn0iLCJleHAiOjE3ODgwODcwMTd9.B6b1Lj-zDf0qUtO_1BXgr9Z1n9fmxl016p7Bkp1zc4sIw6_9-fBYIAJKZ6Tap4hx0E4fgQP9xOIcMuOVj9XGHkpL7Zn9hNH9jhBkBY4IRHjg1Vt21Hd4ZPUYA5zngIKQzt_PciZUj4qEnBxMEB0rtMYWlBp5dBq-Ms2F1gD3cEOi8ZgMRuhV8lxjdqEVL0p5KZ-ePk2ZUrmdYS53sJnsAodpYuSjImhoJlAa5KAW6TNX4ELL8TEAk4uszMbIZCMqhZvgmyKQ-C9QH5xtQMDWKyGovV0zp_yAllXT53J2qsPOQ8fau1hV4Xk0ygTBDRHfNf8tRSWQ7dJr4vKEJ_UOsA";


        String publicKey = decryptUtils.getPublicKeys().get("upload");
        RSAPublicKey rsaPublicKey = decryptUtils.readPublicKey(publicKey);
        Claims claims = decryptUtils.JwtDecrypt(token, rsaPublicKey);
        String json = claims.get("upload-info", String.class);

        System.out.println(json);

    }


}
