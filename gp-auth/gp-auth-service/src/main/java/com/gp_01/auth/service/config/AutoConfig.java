package com.gp_01.auth.service.config;

import com.aliyun.auth.credentials.Credential;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AutoConfig {





    @Bean
    public Credential credential (AliyunConfigProperties properties){
        return Credential.builder().accessKeyId(properties.getAccessKeyId())
                .accessKeySecret(properties.getAccessKeySecret()).build();
    }


    @Bean
    public RedisTemplate<String, byte[]> redisBytesTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        // ByteArrayRedisSerializer 序列化/反序列化出来是 byte[]，正好匹配泛型
        RedisSerializer<byte[]> redisSerializer = RedisSerializer.byteArray();
        template.setValueSerializer(redisSerializer);
        template.setHashValueSerializer(redisSerializer);
        template.afterPropertiesSet();
        return template;
    }
}
