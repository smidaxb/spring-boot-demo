package com.xkcoding.cache.redis.aop;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisNonblockingLock {
    String key() default "";
    String message() default "获取Redis分布式锁异常";
    int expireTime() default 3600;
}
