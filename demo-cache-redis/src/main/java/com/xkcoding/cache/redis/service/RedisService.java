package com.xkcoding.cache.redis.service;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RedisService {

    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);

    @Autowired
    private ShardedJedisPool shardedJedisPool;


    public void put(String key, String value) {
        ShardedJedis client = null;
        try {
            client = getRedisClient();
            client.set(key, value);
        } catch (Exception e) {
            logger.error("put|存储键值对错误|", e);
        } finally {
            close(client);
        }
    }

    public String getStr(String key) {
        ShardedJedis client = null;
        String value = null;
        try {
            client = getRedisClient();
            value = client.get(key);
        } catch (Exception e) {
            logger.error("getStr|查询Value错误|", e);
            return value;
        } finally {
            close(client);
        }
        return value;
    }


    /**
     * 缓存失效的降级策略
     */
    public Object getStrFallback(String key) {
        logger.warn("getStrFallback|缓存失效|已降级|key:{}", key);
        return null;
    }


    public Long ttl(String key) {
        ShardedJedis client = null;
        Long ttl = (long) -1;
        try {
            client = getRedisClient();
            ttl = client.ttl(key);
        } catch (Exception e) {
            logger.error("ttl|查询超时时间错误|", e);
            return ttl;
        } finally {
            close(client);
        }
        return ttl;
    }


    public Long expire(String key, int seconds) {
        ShardedJedis client = null;
        Long res = (long) -1;
        try {
            client = getRedisClient();
            res = client.expire(key, seconds);
        } catch (Exception e) {
            logger.error("expire|设置超时时间错误|", e);
            return res;
        } finally {
            close(client);
        }
        return res;
    }


    public Long getNumber(String key) {
        ShardedJedis client = null;
        Long res = null;
        try {
            client = getRedisClient();
            String str = client.get(key);
            if (StringUtils.isEmpty(str)) {
                res = (long) 0;
            } else {
                res = Long.parseLong(str);
            }
        } catch (Exception e) {
            logger.error("getNumber|查询数值类型错误|", e);
            return res;
        } finally {
            close(client);
        }
        return res;
    }

    public Integer getNumberInt(String key) {
        ShardedJedis client = null;
        Integer res = null;
        try {
            client = getRedisClient();
            String str = client.get(key);
            if (StringUtils.isEmpty(str)) {
                res = 0;
            } else {
                res = Integer.parseInt(str);
            }
        } catch (Exception e) {
            logger.error("getNumber|查询数值类型错误|", e);
            res = 0;
        } finally {
            close(client);
        }
        return res;
    }

    public Long setnx(String key, Object value) {
        ShardedJedis client = null;
        Long res = (long) 0;
        try {
            client = getRedisClient();
            Gson gson = new Gson();
            res = client.setnx(key, gson.toJson(value));
        } catch (Exception e) {
            logger.error("setnx|错误|", e);
            return res;
        } finally {
            close(client);
        }
        return res;
    }

    public Long setnx(String key, Object value, int seconds) {
        ShardedJedis client = null;
        Long res = (long) 0;
        Gson gson = new Gson();
        try {
            client = getRedisClient();
            res = client.setnx(key, gson.toJson(value));
            if (1 == res) {
                client.expire(key, seconds);
            }
        } catch (Exception e) {
            logger.error("setnx|错误|", e);
            return res;
        } finally {
            close(client);
        }
        return res;
    }


    public void put(String key, Object value) {
        ShardedJedis client = null;
        Gson gson = new Gson();
        try {
            client = getRedisClient();
            client.set(key, gson.toJson(value));
        } catch (Exception e) {
            logger.error("put|存储对象错误|", e);
        } finally {
            close(client);
        }
    }


    public boolean putFallback(String key, Object value, int seconds) {
        //do nothing
        return Boolean.FALSE;
    }


    public boolean put(String key, String value, int seconds) {
        ShardedJedis client = null;
        try {
            client = getRedisClient();
            client.set(key, value);
            client.expire(key, seconds);
        } catch (Exception e) {
            logger.error("put|存储对象并设置超时时间错误|", e);
            return Boolean.FALSE;
        } finally {
            close(client);
        }
        return Boolean.TRUE;
    }


    public Object getObject(String key, Class clazz) {
        ShardedJedis client = null;
        Gson gson = new Gson();
        String json = null;
        try {
            client = getRedisClient();
            json = client.get(key);
        } catch (Exception e) {
            logger.error("getObject|调用Redis异常|", e);
            throw new RuntimeException("Redis 服务异常");
        } finally {
            close(client);
        }
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        Object object = null;
        try {
            object = gson.fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            logger.error("getObject|解析对象出错|", e);
            return null;
        }

        return object;
    }

    /**
     * 缓存失效的降级策略
     */
    public Object getObjectFallback(String key, Class clazz) {
        logger.warn("getObjectFallback|缓存失效|已降级|key:{}", key);
        return null;
    }


    public long incre(String key, long index) {
        ShardedJedis client = null;
        long num = 0;
        try {
            client = getRedisClient();
            num = client.incrBy(key, index);
        } catch (Exception e) {
            logger.error("incre|原子加错误|", e);
        } finally {
            close(client);
        }
        return num;
    }


    public void remove(String key) {
        ShardedJedis client = null;
        try {
            client = getRedisClient();
            client.del(key);
        } catch (Exception e) {
            logger.error("remove|删除KEY错误|", e);
        } finally {
            close(client);
        }
    }


    public boolean exist(String key) {
        ShardedJedis client = null;
        boolean res = Boolean.FALSE;
        try {
            client = getRedisClient();
            res = client.exists(key);
        } catch (Exception e) {
            logger.error("exist|查询KEY是否存在错误|", e);
        } finally {
            close(client);
        }
        return res;
    }

    public boolean hexists(String key, String field) {
        ShardedJedis client = null;
        boolean res = Boolean.FALSE;
        try {
            client = getRedisClient();
            res = client.hexists(key, field).booleanValue();
        } catch (Exception e) {
            logger.error("hexists|查询错误|", e);
        } finally {
            close(client);
        }
        return res;
    }

    public Set<String> hkeys(String key) {
        ShardedJedis client = null;
        Set<String> res = null;
        try {
            client = getRedisClient();
            res = client.hkeys(key);
        } catch (Exception e) {
            logger.error("hkeys|查询键集合错误|", e);
            res = new HashSet<>();
        } finally {
            close(client);
        }
        return res;
    }

    public Long hset(String key, String field, String value) {
        ShardedJedis client = null;
        long res = 0L;
        try {
            client = getRedisClient();
            res = client.hset(key, field, value).longValue();
        } catch (Exception e) {
            logger.error("hset|设置散列错误|", e);
        } finally {
            close(client);
        }
        return Long.valueOf(res);
    }

    public String hget(String key, String field) {
        ShardedJedis client = null;
        String res = null;
        try {
            client = getRedisClient();
            res = client.hget(key, field);
        } catch (Exception e) {
            logger.error("hget|查询散列值错误|", e);
        } finally {
            close(client);
        }
        return res;
    }

    public void hput(String key, String field, Object value) {
        Gson gson = new Gson();
        hset(key, field, gson.toJson(value));
    }


    public Object hgetObject(String key, String field, Class clazz) {
        ShardedJedis client = null;
        String jsonStr = null;
        try {
            client = getRedisClient();
            jsonStr = client.hget(key, field);
        } catch (Exception e) {
            logger.error("hgetObject|查询散列对象错误|", e);
            return null;
        } finally {
            close(client);
        }
        if (StringUtils.isEmpty(jsonStr)) {
            logger.warn("hgetObject|查询散列对象为null|key:{}", key);
            return null;
        }
        Gson gson = new Gson();
        Object object = null;
        try {
            object = gson.fromJson(jsonStr, clazz);
        } catch (JsonSyntaxException e) {
            logger.error("hgetObject|解析对象出错|key:{}|clazz:{}", key, clazz, e);
            return null;
        }
        return object;
    }


    public long hdel(String key, String... field) {
        ShardedJedis client = null;
        long res = 0L;
        try {
            client = getRedisClient();
            res = client.hdel(key, field).longValue();
        } catch (Exception e) {
            logger.error("hdel|删除散列对象错误|key:{}", key, e);
        } finally {
            close(client);
        }
        return res;
    }


    public Long sAdd(String key, List<String> values) {
        ShardedJedis client = null;
        Long res = null;
        try {
            client = getRedisClient();
            for (String value : values) {
                res = client.sadd(key, value);
            }
        } catch (Exception e) {
            logger.error("sAdd|错误|key:{}", key, e);
        } finally {
            close(client);
        }
        return res;
    }

    public Set<String> sMembers(String key) {
        ShardedJedis client = null;
        Set<String> res = null;
        try {
            client = getRedisClient();
            res = client.smembers(key);
        } catch (Exception e) {
            logger.error("sMembers|错误|key:{}", key, e);
        } finally {
            close(client);
        }
        return res;
    }

    public Long sAdd(String key, String value) {
        ShardedJedis client = null;
        Long res = null;
        try {
            client = getRedisClient();
            res = client.sadd(key, value);
        } catch (Exception e) {
            logger.error("sAdd|错误|key:{}|value:{}", key, value, e);
        } finally {
            close(client);
        }
        return res;
    }

    /*
     *获取含有泛型的对象
     */

//    public Object getTypeObject(String key, Class clazz, Class genericTypes) {
//        ShardedJedis client = null;
//        String jsonStr = null;
//        try {
//            client = getRedisClient();
//            jsonStr = client.get(key);
//        } catch (Exception e) {
//            logger.error("getTypeObject|获取含有泛型的对象|错误|key:{}", key, e);
//            return null;
//        } finally {
//            close(client);
//        }
//        if (StringUtils.isEmpty(jsonStr)) {
//            logger.warn("getTypeObject|对象为空|key:{}", key);
//            return null;
//        }
//        Type type = new ParameterizeTypeImpl(clazz, new Class[]{genericTypes});
//        Gson gson = new Gson();
//        Object object = null;
//        try {
//            object = gson.fromJson(jsonStr, type);
//        } catch (JsonSyntaxException e) {
//            logger.error("getTypeObject|解析错误|key:{}|clazz:{}", key, clazz, e);
//            return null;
//        }
//        return object;
//    }

    /**
     * 获取Redis连接
     *
     * @return
     */
    public ShardedJedis getRedisClient() {
        ShardedJedis client = null;
        try {
            client = shardedJedisPool.getResource();
        } catch (Exception e) {
            logger.error("getRedisCli|获取Redis连接异常|", e);
            close(client);

        }
        if (shardedJedisPool.getNumIdle() < 3) {
            logger.warn("getRedisClient|pool warn|redis idle count:{}", shardedJedisPool.getNumIdle());
        }
        return client;
    }

    /**
     * 回收连接
     * 不抛出任何异常
     */
    private void close(ShardedJedis client) {
        if (null == client) {
            logger.warn("close|连接为空");
            return;
        }
        try {
            client.close();
        } catch (Exception ignore) {
            logger.error("close|回收连接错误|", ignore);
        }

    }
}
