package com.gp_01.file.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RedisUtils {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 写入值
     */
    public void set(String key, String value){
        stringRedisTemplate.opsForValue().set(key, value);
    }

    /**
     * 获取值
     */
    public String get(String key){
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 写入值，并设置过期时间
     *
     * @param second 单位秒 设置负数为永久不过期
     */
    public void set(String key, String value, Long second){
        if(second > 0){
            stringRedisTemplate.opsForValue().set(key, value, second);
        } else {
            set(key,value);
        }
    }

    /**
     * 自增1
     * @return 增加后的值
     */
    public Long increment(String key){
        return stringRedisTemplate.opsForValue().increment(key);
    }

    /**
     * 判断当前key是否存在
     */
    public boolean hasKey(String key){
        return stringRedisTemplate.hasKey(key);
    }

    /**
     * 删除key
     */
    public void deletedKey(String key){
        stringRedisTemplate.delete(key);
    }

    /**
     * 写值到位图
     */
    public void setBitMap(String key, Long offset, boolean value){
        stringRedisTemplate.opsForValue().setBit(key, offset, value);
    }

    /**
     * 从位图获取值
     */
    public Boolean getBitMap(String key, Long offset){
        return stringRedisTemplate.opsForValue().getBit(key,offset);
    }

    /**
     * 统计位图为1的个数
     */
    public Long countBitMap(String key){
        return stringRedisTemplate.execute(RedisConnection::stringCommands).bitCount(key.getBytes());

    }
}

