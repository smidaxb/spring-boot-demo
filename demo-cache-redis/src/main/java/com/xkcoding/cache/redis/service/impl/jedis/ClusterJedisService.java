package com.xkcoding.cache.redis.service.impl.jedis;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.xkcoding.cache.redis.service.JedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.params.SetParams;

import java.util.Set;

/**
 * Jedis集群模式
 */
public class ClusterJedisService implements JedisService {
    private static final Logger logger = LoggerFactory.getLogger(ShardJedisService.class);

    private JedisCluster jedisCluster;

    public ClusterJedisService(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }

    @Override
    public void set(String key, String value) {
        jedisCluster.set(key, value);
    }

    @Override
    public String setWithEx(String key, String value, int seconds) {
        return jedisCluster.setex(key, seconds, value);
    }

    @Override
    public Long setnx(String key, String value) {
        return jedisCluster.setnx(key, value);
    }

    @Override
    public String setNxWithEx(String key, String value, int seconds) {
        return jedisCluster.set(key, value, SetParams.setParams().nx().ex(seconds));
    }

    @Override
    public String getStr(String key) {
        return jedisCluster.get(key);
    }

    @Override
    public Long ttl(String key) {
        return jedisCluster.ttl(key);
    }

    @Override
    public Long expire(String key, int seconds) {
        return jedisCluster.expire(key, seconds);
    }

    @Override
    public Long getNumber(String key) {
        Long res = 0L;
        try {
            res = Long.valueOf(jedisCluster.get(key));
        } catch (Exception e) {
            logger.error("getNumber|查询数值类型错误|", e);
            return res;
        }
        return res;
    }

    @Override
    public Integer getNumberInt(String key) {
        Integer res = 0;
        try {
            res = Integer.valueOf(jedisCluster.get(key));
        } catch (Exception e) {
            logger.error("getNumberInt|查询数值类型错误|", e);
            return res;
        }
        return res;
    }

    @Override
    public long incre(String key, long index) {
        long res = 0;
        try {
            res = jedisCluster.incrBy(key, index);
        } catch (Exception e) {
            logger.error("incre|原子加错误|", e);
        }
        return res;
    }

    @Override
    public <T> T getObject(String key, Class<T> clazz) {
        Gson gson = new Gson();
        String json = jedisCluster.get(key);
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        T object = null;
        try {
            object = gson.fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            logger.error("getObject|解析对象出错|", e);
            return null;
        }

        return object;
    }

    @Override
    public void remove(String key) {
        jedisCluster.del(key);
    }

    @Override
    public boolean exist(String key) {
        return jedisCluster.exists(key);
    }

    @Override
    public boolean hexists(String key, String field) {
        return jedisCluster.hexists(key, field);
    }

    @Override
    public Set<String> hkeys(String key) {
        return jedisCluster.hkeys(key);
    }

    @Override
    public Long hset(String key, String field, String value) {
        return jedisCluster.hset(key, field, value);
    }

    @Override
    public String hget(String key, String field) {
        return jedisCluster.hget(key, field);
    }

    @Override
    public long hdel(String key, String... field) {
        return jedisCluster.hdel(key, field);
    }

    @Override
    public Set<String> sMembers(String key) {
        return jedisCluster.smembers(key);
    }

    @Override
    public Long sAdd(String key, String value) {
        return jedisCluster.sadd(key, value);
    }
}
