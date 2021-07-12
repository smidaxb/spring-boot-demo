package com.xkcoding.cache.redis.service;

import com.xkcoding.cache.redis.aop.RedisLockInfo;
import redis.clients.jedis.JedisPubSub;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface JedisService {

    public void set(String key, String value);

    public String setWithEx(String key, String value, int seconds);

    public Long setnx(String key, String value);

    public String setNxWithEx(String key, String value, int seconds);

    public String getStr(final String key);

    public Long ttl(final String key);

    public Long expire(final String key, final int seconds);

    public Long getNumber(String key);

    public Integer getNumberInt(String key);

    public long incre(String key, long index);

    public void remove(String key);

    public boolean exist(String key);

    public boolean hexists(String key, String field);

    public Set<String> hkeys(String key);

    public Long hset(final String key, final String field, final String value);

    public String hget(String key, String field);

    public long hdel(String key, String... field);

    public Set<String> sMembers(String key);

    public Long sAdd(String key, String value);

    public <T> T getObject(String key, Class<T> clazz);

//    getObject(String key, Class<RedisLockInfo> redisLockInfoClass);
}
