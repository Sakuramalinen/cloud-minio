package com.gp_01.auth.service.sdk;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dypnsapi20170525.models.*;
import com.google.gson.Gson;
import com.gp_01.auth.model.enums.VerificationCodeType;
import darabonba.core.client.ClientOverrideConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageVerifier {

    private final Credential credential;


    public String send(String phoneNumber, VerificationCodeType codeType) {

        StaticCredentialProvider staticCredentialProvider = StaticCredentialProvider.create(credential);

        try (AsyncClient client = AsyncClient.builder()
                .region("cn-beijing")
                .credentialsProvider(staticCredentialProvider)
                .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                                .setEndpointOverride("dypnsapi.aliyuncs.com")
                )
                .build()) {

            SendSmsVerifyCodeRequest sendSmsVerifyCodeRequest = SendSmsVerifyCodeRequest.builder()
                    //短信接收方手机号
                    .phoneNumber(phoneNumber)
                    //签名名称
                    .signName("恒创联众")
                    //短信模板code
                    .templateCode(codeType.getCode())
                    //短信模板参数
                    .templateParam("{\"code\":\"##code##\",\"min\":\"5\"}")
                    //验证码长度支持4～8位长度 默认4
                    .codeLength(6L)
                    //验证码有效时长, 单位秒 默认300
                    .validTime(300L)
                    //核验规则（同号码重复发送验证码处理方式） 1.覆盖处理 2.保留 默认1
                    .duplicatePolicy(2L)
                    //时间间隔（同号码间隔多久可以再次发送验证码）单位秒 默认60
                    .interval(60L)
                    //验证码生成方式 1.纯数字 2.纯大写字母 3.纯小写字母 4.大小写字母混合 5.数字+大写字母混合 6.数字+小写字母混合 7.数字+大小写字母混合 默认1
                    .codeType(1L)
                    //是否返回验证码 true返回，false不返回 默认true
                    .returnVerifyCode(true)
                    .build();

            CompletableFuture<SendSmsVerifyCodeResponse> response = client.sendSmsVerifyCode(sendSmsVerifyCodeRequest);
            SendSmsVerifyCodeResponse resp = null;
            try {
                resp = response.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
//            return new Gson().toJson(resp);

            return resp.getBody().getModel().getVerifyCode();
        }
    }


    public boolean check(String phoneNumber , String code){
        StaticCredentialProvider staticCredentialProvider = StaticCredentialProvider.create(credential);

        try (AsyncClient client = AsyncClient.builder()
                .region("cn-beijing") // Region ID
                .credentialsProvider(staticCredentialProvider)
                // Client-level configuration rewrite, can set Endpoint, Http request parameters, etc.
                .overrideConfiguration(
                        ClientOverrideConfiguration.create()

                                .setEndpointOverride("dypnsapi.aliyuncs.com")
                )
                .build()) {

            // Parameter settings for API request
            CheckSmsVerifyCodeRequest checkSmsVerifyCodeRequest = CheckSmsVerifyCodeRequest.builder()
                    .phoneNumber(phoneNumber)
                    .verifyCode(code)
                    //是否区分大小写 1.不区分 2.区分 默认1
                    .caseAuthPolicy(1L)
                    .build();

            CompletableFuture<CheckSmsVerifyCodeResponse> response = client.checkSmsVerifyCode(checkSmsVerifyCodeRequest);

            CheckSmsVerifyCodeResponse resp = null;
            try {
                resp = response.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
//            System.out.println(new Gson().toJson(resp));

            return resp.getBody().getModel().getVerifyResult().equals("PASS");

        }
    }
}
