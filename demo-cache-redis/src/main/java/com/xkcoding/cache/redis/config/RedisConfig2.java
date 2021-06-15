package com.xkcoding.cache.redis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

//@Configuration
//@EnableCaching
public class RedisConfig2 extends CachingConfigurerSupport {

    @Value("${redisLuna.host}")
    private String host;
    @Value("${redisLuna.port}")
    private int port;
    @Value("${redisLuna.password}")
    private String password;
    @Value("${redisLuna.pool.max-active}")
    private int maxActive;
    @Value("${redisLuna.pool.max-wait}")
    private int maxWait;
    @Value("${redisLuna.pool.max-idle}")
    private int maxIdle;
    @Value("${redisLuna.pool.min-idle}")
    private int mainIdle;
    @Value("${redisLuna.timeout}")
    private int timeout;

    /**
     * 配置JedisPoolConfig
     *
     * @return JedisPoolConfig实体
     */
    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(this.maxIdle);
        jedisPoolConfig.setMaxWaitMillis(this.maxWait);
        jedisPoolConfig.setMinIdle(this.mainIdle);
        return jedisPoolConfig;
    }

    /**
     * RedisConnectionFactory配置
     *
     * @return
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // redisStandaloneConfiguration
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(this.host, this.port);
        redisStandaloneConfiguration.setPassword(this.password);

        // jedisClientConfiguration
        JedisClientConfiguration.DefaultJedisClientConfigurationBuilder builder = (JedisClientConfiguration.DefaultJedisClientConfigurationBuilder) JedisClientConfiguration.builder();
        builder.poolConfig(this.jedisPoolConfig());
        builder.usePooling();
        builder.connectTimeout(Duration.ofMillis(this.timeout));
        builder.readTimeout(Duration.ofMillis(this.timeout));
        JedisClientConfiguration jedisClientConfiguration = builder.build();

        // redisConnectionFactory
        return new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration);
    }

    /**
     * 配置StringRedisTemplate实体
     *
     * @return
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(this.redisConnectionFactory());
        return stringRedisTemplate;
    }

    /**
     * 配置RedisTemplate实体
     *
     * @return
     */
    @Bean
    public RedisTemplate redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(this.redisConnectionFactory());
        // 配置序列化方式
        redisTemplate.setKeySerializer(keySerializer());
        redisTemplate.setHashKeySerializer(keySerializer());
        redisTemplate.setValueSerializer(valueSerializer());
        redisTemplate.setHashValueSerializer(valueSerializer());
        return redisTemplate;
    }

    /**
     * key序列化
     *
     * @return
     */
    private RedisSerializer<String> keySerializer() {
        return new StringRedisSerializer();
    }

    /**
     * value序列化
     *
     * @return
     */
    private RedisSerializer<Object> valueSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }
}
