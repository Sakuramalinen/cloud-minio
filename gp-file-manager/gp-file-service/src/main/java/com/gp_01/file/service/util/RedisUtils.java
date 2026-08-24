package com.gp_01.file.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.cloud.bootstrap.encrypt.KeyProperties;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
@Slf4j
public class RedisUtils {

    private final StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper;

    private final RedissonClient redissonClient;
    private final KeyProperties keyProperties;

    /**
     * 写入值
     */
    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    /**
     * 写入值，并设置过期时间
     * 设置负数为永久不过期
     */
    public void set(String key, String value, Long expiry, TimeUnit timeUnit) {
        if (expiry > 0) {
            stringRedisTemplate.opsForValue().set(key, value, expiry, timeUnit);
        } else {
            set(key, value);
        }
    }

    /**
     * 批量写入，设置过期时间
     * 过期时间一致
     */
    public void setBatch(Map<String, String> map, Long expiry, TimeUnit timeUnit) {
        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            //序列化器
            RedisSerializer<String> stringSerializer = stringRedisTemplate.getStringSerializer();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                byte[] keyByte = stringSerializer.serialize(entry.getKey());
                byte[] valueByte = stringSerializer.serialize(entry.getValue());
                if (keyByte != null && valueByte != null) {
                    Expiration expiration = Expiration.from(expiry, timeUnit);
                    connection.stringCommands().set(keyByte, valueByte, expiration, RedisStringCommands.SetOption.upsert());
                }
            }
            return null;
        });
    }

    /**
     * 写对象
     */
    public void setObject(String key, Object value, long expiry, TimeUnit timeUnit) {
        try {
            String jsonStr = objectMapper.writeValueAsString(value);
            log.debug("写入 -> {}", jsonStr);
            stringRedisTemplate.opsForValue().set(key, jsonStr, expiry, timeUnit);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("redis序列化失败", e);
        }
    }

    /**
     * 获取值
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 获取对象并续期
     */
    public <T> T getObjectAndReNew(String key, Class<T> tClass, Duration timeOut) {
        try {
            String jsonStr = stringRedisTemplate.opsForValue().getAndExpire(key, timeOut);
            log.debug("读取 -> {}", jsonStr);
            return objectMapper.readValue(jsonStr, tClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("redis反序列化失败", e);
        }
    }

    /**
     * 获取并删除对象
     *
     * @param key
     * @param tClass
     * @param <T>
     * @return
     */
    public <T> T getDelObject(String key, Class<T> tClass) {
        String jsonStr = stringRedisTemplate.opsForValue().getAndDelete(key);
        try {
            return objectMapper.readValue(jsonStr, tClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取值后删除
     */
    public String getDel(String key) {
        return stringRedisTemplate.opsForValue().getAndDelete(key);
    }

    /*
     * 写hash
     */
    public void setHashAll(String key, Map<String, String> map, Long expiry, TimeUnit unit) {
        stringRedisTemplate.opsForHash().putAll(key, map);
        stringRedisTemplate.expire(key,expiry, unit);
    }

    /**
     * 获取全部hash
     */
    public Map<Object, Object> getHashAll(String key) {
        return stringRedisTemplate.opsForHash().entries(key);
    }

    public List<Map<String, String>> getHashAllBatch(List<String> keys) {
        List<Object> resList = stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (String key : keys) {
                byte[] keyBytes = key.getBytes();
                connection.hashCommands().hGetAll(keyBytes);
            }
            return null;
        });
        List<Map<String, String>> finalList = new ArrayList<>(resList.size());
        for (Object res : resList) {
            Map<String, String> finalMap = (Map<String, String>)res;
            finalList.add(finalMap);
        }
        return finalList;
    }

    /**
     * 获取hash后删除
     */
    public Map<String, String> getDelHashAll(String key) {
        RedisSerializer<String> stringSerializer = stringRedisTemplate.getStringSerializer();
        List<Object> resList = stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            byte[] byteKey = stringSerializer.serialize(key);
            //获取key
            connection.hashCommands().hGetAll(byteKey);
            //删除key
            connection.keyCommands().del(byteKey);
            return null;
        });

        Map<String, String> rawMap = (Map<String, String>) resList.get(0);
        Map<String, String> resultMap = new HashMap<>();

        if (rawMap != null && !rawMap.isEmpty()) {
            for (Map.Entry<String, String> entry : rawMap.entrySet()) {
                String field = entry.getKey();
                String value = entry.getValue();
                resultMap.put(field, value);
            }
        }

        return resultMap;
    }


    /**
     * 自增1
     *
     * @return 增加后的值
     */
    public Long increment(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }

    /**
     * 判断当前key是否存在
     */
    public boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    /**
     * 删除key
     */
    public void deletedKey(String key) {
        stringRedisTemplate.delete(key);
    }

    /**
     * 写值到位图
     */
    public void setBitMap(String key, Long offset, boolean value) {
        stringRedisTemplate.opsForValue().setBit(key, offset, value);
    }

    /**
     * 从位图获取值
     */
    public Boolean getBitMap(String key, Long offset) {
        return stringRedisTemplate.opsForValue().getBit(key, offset);
    }

    /**
     * 统计位图为1的个数
     */
    public Long countBitMap(String key) {
        return stringRedisTemplate.execute(RedisConnection::stringCommands).bitCount(key.getBytes());
    }


}

