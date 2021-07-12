package com.xkcoding.cache.redis.service.impl.jedis;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.xkcoding.cache.redis.service.JedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.params.SetParams;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class ShardJedisService implements JedisService {

    private static final Logger logger = LoggerFactory.getLogger(ShardJedisService.class);
    private ShardedJedisPool shardedJedisPool;

    public ShardJedisService(ShardedJedisPool shardedJedisPool) {
        this.shardedJedisPool = shardedJedisPool;
    }

    @Override
    public void set(String key, String value) {
        ShardedJedis client = null;
        try {
            client = getRedisClient();
            client.set(key, value);
        } catch (Exception e) {
            logger.error("set|存储键值对错误|", e);
        } finally {
            close(client);
        }
    }

    @Override
    public Long setnx(String key, String value) {
        ShardedJedis client = null;
        Long res = (long) 0;
        try {
            client = getRedisClient();
            res = client.setnx(key, value);
        } catch (Exception e) {
            logger.error("setnx|错误|", e);
            return res;
        } finally {
            close(client);
        }
        return res;
    }

    @Override
    public String setWithEx(String key, String value, int seconds) {
        ShardedJedis client = null;
        String res = null;
        try {
            client = getRedisClient();
            res = client.setex(key, seconds, value);
        } catch (Exception e) {
            logger.error("setWithEx|错误|", e);
            return res;
        } finally {
            close(client);
        }
        return res;
    }

    /**
     * EX second ：设置键的过期时间为 second 秒。 SET key value EX second 效果等同于 SETEX key second value 。
     * PX millisecond ：设置键的过期时间为 millisecond 毫秒。 SET key value PX millisecond 效果等同于 PSETEX key millisecond value 。
     * NX ：只在键不存在时，才对键进行设置操作。 SET key value NX 效果等同于 SETNX key value 。
     * XX ：只在键已经存在时，才对键进行设置操作。
     */
    @Override
    public String setNxWithEx(String key, String value, int seconds) {
        ShardedJedis client = null;
        String res = null;
        try {
            client = getRedisClient();
            client.set(key, value, SetParams.setParams().nx().ex(seconds));
        } catch (Exception e) {
            logger.error("setNxWithEx|错误|", e);
            return res;
        } finally {
            close(client);
        }
        return res;
    }

    @Override
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

    @Override
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


    @Override
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


    @Override
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

    @Override
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

    @Override
    public <T> T getObject(String key, Class<T> clazz) {
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


    @Override
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


    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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
