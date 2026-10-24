package com.example.rbacdemo.common;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void setList (String key, List<String> list, long timeoutMinutes){
        String value = String.join(",",list);
        redisTemplate.opsForValue().set(key,value,timeoutMinutes, TimeUnit.MINUTES);
    }

    public List<String> getList (String key){
        String value = redisTemplate.opsForValue().get(key);
        if(value==null || value.isEmpty()){
            return null;
        }
        return List.of(value.split(","));
    }

    public void delete(String key){
        redisTemplate.delete(key);
    }

    public void deleteByPattern(String pattern){
        Set<String> keys = redisTemplate.keys(pattern);
        if(keys!=null&&!keys.isEmpty()){
            redisTemplate.delete(keys);
        }
    }

    public void set(String key,String value,long timeoutMinutes){
        redisTemplate.opsForValue().set(key,value,timeoutMinutes,TimeUnit.MINUTES);
    }

    public boolean exists(String key){
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
