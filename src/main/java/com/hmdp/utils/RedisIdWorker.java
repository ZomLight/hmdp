package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdWorker {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    //基准时间戳
    private static final long BEGIN_TIMESTAMP = 1704067200L;

    private final int COUNT_BITS = 32 ;

    //当前线程对应的时间
    private String date;

    /***
     * @description: TODO :利用redis自增长完成全局唯一id，返回值long，8个字节64位
     * @params: [keyPrefix]
     * @return: java.lang.Long
     * @author: SenGang
     */
    public Long nextId(String keyPrefix){
        LocalDateTime now = LocalDateTime.now();
        long curTime = now.toEpochSecond(ZoneOffset.UTC);
        long timeStamp = curTime - BEGIN_TIMESTAMP;

        date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));

        Long increment = stringRedisTemplate.opsForValue().increment("icr" + keyPrefix + date);

        return timeStamp<<COUNT_BITS|increment;
    }

    public void delPreId(String keyPrefix){
        // 订单添加失败,总数-1
        stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + date,-1L);
    }
}
