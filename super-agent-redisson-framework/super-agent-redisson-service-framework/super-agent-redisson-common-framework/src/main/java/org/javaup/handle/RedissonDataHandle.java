package org.javaup.handle;

import lombok.AllArgsConstructor;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @program: 企业级别深度设计 AI Agent。添加 阿星不是程序员 微信，添加时备注 super 来获取项目的完整资料
 * @description: redisson操作
 * @author: 阿星不是程序员
 **/
@AllArgsConstructor
public class RedissonDataHandle {

    private final RedissonClient redissonClient;

    public String get(String key){
        return (String)redissonClient.getBucket(key).get();
    }

    public void set(String key,String value){
        redissonClient.getBucket(key).set(value);
    }

    public void set(String key,String value,long timeToLive, TimeUnit timeUnit){
        redissonClient.getBucket(key).set(value,getDuration(timeToLive,timeUnit));
    }

    /**
     * 仅当 key 不存在时才设置（原子语义）。
     * <p>
     * 常用于“一次性闸门/幂等标记/简单限流”场景。
     */
    public boolean trySetIfAbsent(String key, String value) {
        return redissonClient.getBucket(key).setIfAbsent(value);
    }

    /**
     * 仅当 key 不存在时才设置，并附带过期时间（原子语义）。
     */
    public boolean trySetIfAbsent(String key, String value, long timeToLive, TimeUnit timeUnit) {
        return redissonClient.getBucket(key).setIfAbsent(value, getDuration(timeToLive, timeUnit));
    }

    public Duration getDuration(long timeToLive, TimeUnit timeUnit){
        switch (timeUnit) {

            case MINUTES -> {
                return Duration.ofMinutes(timeToLive);
            }

            case HOURS -> {
                return Duration.ofHours(timeToLive);
            }

            case DAYS -> {
                return Duration.ofDays(timeToLive);
            }

            default -> {
                return Duration.ofSeconds(timeToLive);
            }
        }
    }
}
