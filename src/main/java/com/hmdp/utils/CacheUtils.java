package com.hmdp.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hmdp.constant.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
@Slf4j
public class CacheUtils {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newCachedThreadPool();

    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value.toString(), time, unit);
    }

    public void setWithLogicExpire(String key, Object value, Long expireTime, TimeUnit unit) {
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(
                LocalDateTime.now().plusSeconds(unit.toSeconds(expireTime))
        );
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    //缓存穿透
    public  <T, ID> T queryWithPassThrough(String keyPrefix, ID id, Class<T> tyoe, Function<ID, T> dbFallBack, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if(StrUtil.isNotBlank(json)) {
            return JSONUtil.toBean(json, tyoe);
        }

        //防止空值
        if(json != null) {
            return null;
        }

        T t = dbFallBack.apply(id);
        if(t == null) {
            //存入空值, 没必要使用template
            stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);

        }

        setWithLogicExpire(key, t, time, unit);
        return t;

    }

    //缓存击穿，互斥锁
    public <T, ID> T queryWithLogicalExpire(String keyPrefix, ID id, Class<T> tyoe, Function<ID, T> dbFallBack, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        String redisDataJson = stringRedisTemplate.opsForValue().get(key);
        if(StrUtil.isBlank(redisDataJson)) {
            return queryWithPassThrough(keyPrefix, id, tyoe, dbFallBack, time, unit);
        }

        RedisData redisData = JSONUtil.toBean(redisDataJson, RedisData.class);
        //data里面存的是 jsonobject，所以要转换
        T t = JSONUtil.toBean((JSONObject) redisData.getData(), tyoe);
        LocalDateTime expireTime = redisData.getExpireTime();

        if(expireTime.isAfter(LocalDateTime.now())) {
            return t;
        }

        //过期了，拿锁
        boolean isLock = tryLock(RedisConstants.LOCK_SHOP_KEY + id);
        try {
            if(isLock) {
                CacheUtils.CACHE_REBUILD_EXECUTOR.submit(() -> {
                    T newT = dbFallBack.apply(id);
                    setWithLogicExpire(key, newT, time, unit);
                });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            unLock(RedisConstants.LOCK_SHOP_KEY + id);
        }

        return t;

    }


    private boolean tryLock(String lockName){
        Boolean ifAbsent = stringRedisTemplate.opsForValue().setIfAbsent(lockName, "1", RedisConstants.LOCK_SHOP_TTL, TimeUnit.MILLISECONDS);
        return BooleanUtil.isTrue(ifAbsent);
    }

    private boolean unLock(String lockName){
        Boolean delete = stringRedisTemplate.delete(lockName);
        return BooleanUtil.isTrue(delete);
    }
}
