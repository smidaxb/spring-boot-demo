package com.xkcoding.cache.redis.config;

import com.xkcoding.cache.redis.service.JedisService;
import com.xkcoding.cache.redis.service.impl.jedis.ClusterJedisService;
import com.xkcoding.cache.redis.service.impl.jedis.PoolJedisService;
import com.xkcoding.cache.redis.service.impl.jedis.ShardJedisService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@EnableCaching
@Data
@ConfigurationProperties(prefix = "jedis")
@Slf4j
public class JedisRedisConfig {

    private static final String STANDALONE = "standalone";
    private static final String CLUSTER = "cluster";
    private static final String SHARD = "shard";
    private String model = STANDALONE;
    private String host = "127.0.0.1";
    private int port = 6379;
    private String instances;
    private String password;
    private int timeout = 2000;
    private int maxTotal = 100;
    private int maxIdle = 8;
    private int minIdle = 0;
    private long maxWaitMillis = 1000;
    private int maxAttempts = 5;
    private boolean testOnBorrow = false;
    private boolean testOnReturn = false;

    /**
     * 配置JedisPoolConfig
     *
     * @return JedisPoolConfig实体
     */
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        jedisPoolConfig.setMinIdle(minIdle);
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPoolConfig.setTestOnBorrow(testOnBorrow);
        jedisPoolConfig.setTestOnReturn(testOnReturn);
        jedisPoolConfig.setTestWhileIdle(Boolean.TRUE);
        jedisPoolConfig.setMinEvictableIdleTimeMillis(60000);
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(30000);
        return jedisPoolConfig;
    }

    /**
     * 返回可直接使用的service类
     *
     * @return
     */
    @Bean("jedisService")
    public JedisService jedisService() {
        JedisService res;
        log.info("jedisService 初始化 start| model:{}", model);
        switch (model) {
            case CLUSTER:
                res = initClusterJedisService();
                break;
            case SHARD:
                res = initShardJedisService();
                break;
            default:
                res = initPoolJedisService();
        }
        log.info("jedisService 初始化 success| model:{}", model);
        return res;
    }

    /**
     * 创建单节点jedis服务
     *
     * @return
     */
    private JedisService initPoolJedisService() {
        JedisPool jedisPool = new JedisPool(jedisPoolConfig(), host, port, timeout, password);
        return new PoolJedisService(jedisPool);
    }

    /**
     * 创建shardJedis服务
     *
     * @return
     */
    private JedisService initShardJedisService() {
        List<JedisShardInfo> list = new ArrayList<>();
        for (String hp : instances.split(";")) {
            String[] hapStr = hp.split(":");
            JedisShardInfo info = new JedisShardInfo(hapStr[0], Integer.valueOf(hapStr[1]));
            list.add(info);
        }
        ShardedJedisPool shardedJedisPool = new ShardedJedisPool(jedisPoolConfig(), list);
        return new ShardJedisService(shardedJedisPool);
    }

    /**
     * 创建集群jedis服务
     *
     * @return
     */
    private JedisService initClusterJedisService() {
        Set<HostAndPort> hps = new HashSet<>();
        for (String hp : instances.split(";")) {
            String[] hapStr = hp.split(":");
            HostAndPort hap = new HostAndPort(hapStr[0], Integer.valueOf(hapStr[1]));
            hps.add(hap);
        }
        JedisCluster jedisCluster = new JedisCluster(hps, timeout, timeout, maxAttempts, password, jedisPoolConfig());
        return new ClusterJedisService(jedisCluster);
    }
}
