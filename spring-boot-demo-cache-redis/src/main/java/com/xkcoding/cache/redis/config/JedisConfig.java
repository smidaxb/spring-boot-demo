package com.xkcoding.cache.redis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by yusong on 2018/1/22.
 * Redis配置
 */
@Configuration
public class JedisConfig {

    //jedis连接池
    private static JedisPoolConfig jedisPoolConfig;

    static {
        jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(50);
        jedisPoolConfig.setMaxIdle(10);
        jedisPoolConfig.setMinIdle(5);
        jedisPoolConfig.setMaxWaitMillis(3000);
        jedisPoolConfig.setMinEvictableIdleTimeMillis(60000);
        jedisPoolConfig.setTestWhileIdle(Boolean.TRUE);
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(30000);
        jedisPoolConfig.setTestOnBorrow(Boolean.FALSE);
    }

    @Value("${redis.server.host}")
    private String host;

    @Value("${redis.server.port}")
    private Integer port;

    @Value("${redis.server.password}")
    private String password;

    @Bean(name = "shardedJedisPool")
    public ShardedJedisPool shardedJedisPool() {
        List<JedisShardInfo> nodes = new ArrayList<>();
        JedisShardInfo master = new JedisShardInfo(host, port, "master");
        if (!StringUtils.isEmpty(password)) {
            master.setPassword(password);
        }
        nodes.add(master);
        ShardedJedisPool shardedJedisPool = new ShardedJedisPool(jedisPoolConfig, nodes);
        return shardedJedisPool;
    }

    @Bean(name = "shardedJedis")
    public ShardedJedis shardedJedis() {
        return shardedJedisPool().getResource();
    }


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String pass) {
        this.password = pass;
    }
}
