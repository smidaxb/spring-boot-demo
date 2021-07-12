package com.xkcoding.cache.redis.aop;

import com.google.gson.Gson;
import com.xkcoding.cache.redis.service.JedisService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Date;

@Component
@Aspect
public class RedisLockAspect {
    private static final Logger logger = LoggerFactory.getLogger(RedisLockAspect.class);
    @Resource
    private JedisService jedisService;
    /**
     * 拿到锁的值
     */
    private final static String LOCKED_SUCCESS = "ok";
    /**
     * 阻塞锁最大等待次数
     */
    private final static int MAX_BLOCKING_TIMES = 59;
    /**
     * 机器IP地址
     */
    private final static String IP = IPUtil.getLocalHost();

    /**
     * 阻塞分布式锁
     */
    @Around("@annotation(com.xkcoding.cache.redis.aop.RedisBlockingLock)")
    public Object redisBlockingLock(ProceedingJoinPoint pjp) {
        long start = System.currentTimeMillis();

        Method method = AopUtil.getMethod(pjp);
        String methName = method.getName();
        RedisBlockingLock blockingLock = method.getAnnotation(RedisBlockingLock.class);
        String key = blockingLock.key();
        //如有ID参数 添加到锁中
        key = AopUtil.addId(key, pjp);
        int expireTime = blockingLock.expireTime();
        RedisLockInfo lockValue = setLockInfo(key, expireTime);
        Gson gson = new Gson();
        String redisReturn;
        String lockValueStr = gson.toJson(lockValue);
        long times = 0;
        //尝试获取Redis锁
        try {
            redisReturn = jedisService.setNxWithEx(key, lockValueStr, expireTime);
            while (!LOCKED_SUCCESS.equals(redisReturn) && times <= MAX_BLOCKING_TIMES) {
                times++;
                redisReturn = jedisService.setNxWithEx(key, lockValueStr, expireTime);
                Thread.sleep(1000);
            }
        } catch (Throwable ignore) {
            logger.error("{}|RedisBlockingLock|获取Redis锁异常|方法不执行|key:{}", methName, key, ignore);
            return null;
        }
        if (!LOCKED_SUCCESS.equals(redisReturn)) {
            RedisLockInfo lockInfo = getLockInfo(key);
            logger.error("{}|RedisBlockingLock|达到最大阻塞次数未获取到锁|逻辑不执行|lockInfo:{}", methName, lockInfo);
            releaseLongHold(lockInfo);
            return null;
        }
        logger.info("{}|RedisBlockingLock|拿到锁开始执行业务|value:{}", methName, lockValue);
        Object result = null;
        //业务代码实现
        try {
            result = pjp.proceed();
        } catch (Throwable e) {
            logger.error("{}|RedisBlockingLock|业务逻辑出错|", methName, e);
        } finally {
            //尝试释放Redis锁
            jedisService.remove(key);
            logger.info("{}|RedisBlockingLock|释放锁|key:{}", methName, key);
        }

        long duration = System.currentTimeMillis() - start;
        if (duration > 3000) {
            logger.warn("{}|RedisBlockingLock|方法执行时间过长|duration:{}", methName, duration);
        }

        return result;
    }


    /**
     * 非阻塞分布式锁
     */
    @Around(value = "@annotation(com.xkcoding.cache.redis.aop.RedisNonblockingLock)")
    public Object redisNonblockingLock(ProceedingJoinPoint pjp) {
        Method method = AopUtil.getMethod(pjp);
        RedisNonblockingLock nonblockingLock = method.getAnnotation(RedisNonblockingLock.class);
        String key = nonblockingLock.key();
        //如有ID参数 添加到锁中
        key = AopUtil.addId(key, pjp);
        int expireTime = nonblockingLock.expireTime();
        String methName = method.getName();

        RedisLockInfo lockValue = setLockInfo(key, expireTime);
        //尝试获取Redis锁
        String redisReturn;
        Gson gson = new Gson();
        try {
            redisReturn = jedisService.setNxWithEx(key, gson.toJson(lockValue), expireTime);
        } catch (Throwable e) {
            logger.error("{}|获取Redis锁异常|", methName, e);
            return null;
        }
        //没有获取到锁，不报异常，打印是哪台机器执行的逻辑即可
        Object result = null;
        if (!LOCKED_SUCCESS.equals(redisReturn)) {
            RedisLockInfo lockedInfo = getLockInfo(key);
            logger.warn("{}|未获取到锁|{}", methName, lockedInfo);
            //检查锁是否过期
            releaseLongHold(lockedInfo);
            return result;
        } else {
            logger.info("{}|拿到锁开始执行业务|value:{}", methName, lockValue);
            //业务代码实现
            try {
                result = pjp.proceed();
            } catch (Throwable e) {
                logger.error("{}|业务执行异常|", methName, e);
            } finally {
                //尝试释放Redis锁
                jedisService.expire(key, 3);
                logger.info("{}|释放锁|key:{}", methName, key);
            }
        }
        return result;
    }

    /**
     * 判断锁是否被长期占用，未释放
     * 此方法不抛出异常
     */
    private void releaseLongHold(RedisLockInfo redisLockInfo) {
        try {
            long startTime = redisLockInfo.getLockTime();
            long now = System.currentTimeMillis();
            long difference = now - startTime;
            long supposeUnlock = redisLockInfo.getTtl() * 1000;
            if (difference > (supposeUnlock * 2)) {
                logger.warn("releaseLongHold|锁占用时间过长，强行释放|{}", redisLockInfo);
                try {
                    jedisService.remove(redisLockInfo.getKey());
                } catch (Exception ignore) {
                    logger.error("releaseLongHold|释放长期占用锁异常|", ignore);
                }
            }
        } catch (Exception ignore) {
            logger.error("releaseLongHold|尝试释放锁异常|", ignore);
        }
    }


    /**
     * 拿到锁后，设置分布式锁中Value的信息
     */
    private RedisLockInfo setLockInfo(String redisKey, int expireSeconds) {
        String threadName = Thread.currentThread().getName();
        RedisLockInfo redisLockInfo = new RedisLockInfo(redisKey, IP, threadName, new Date(), expireSeconds);
        return redisLockInfo;
    }

    /**
     * 获取锁中信息
     */
    private RedisLockInfo getLockInfo(String key) {
        RedisLockInfo value = null;
        try {
            value = jedisService.getObject(key, RedisLockInfo.class);
        } catch (Exception ignore) {
            logger.error("getLockInfo|查询锁信息异常|", ignore);
        }
        if (null == value) {
            logger.warn("getLockInfo|查询数据错误|key:{}", key);
            value = new RedisLockInfo(key);
        }
        return value;
    }


}
