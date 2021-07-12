package com.xkcoding.cache.redis.aop;

import lombok.Data;

import java.io.Serializable;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yusong on 2018/9/12.
 * Redis锁中的信息
 */
@Data
public class RedisLockInfo implements Serializable {
    private static final long serialVersionUID = -8755030544337916720L;
    //锁的Key
    private String key;
    //IP
    private String ip;
    //线程名字
    private String threadName;
    //获得锁时间
    private long lockTime;
    //预计锁时间 单位 秒
    private long ttl;

    public RedisLockInfo(String key) {
        this.key = key;
        this.lockTime = System.currentTimeMillis();
    }

    public RedisLockInfo(String key, String ip, String threadName, Date lockTime, long ttl) {
        this.key = key;
        this.ip = ip;
        this.threadName = threadName;
        this.lockTime = lockTime.getTime();
        this.ttl = ttl;
    }

    @Override
    public String toString() {
        Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "RedisLockInfo{" +
                "key='" + key + '\'' +
                ", ip='" + ip + '\'' +
                ", threadName='" + threadName + '\'' +
                ", time=" + format.format(lockTime) +
                ", ttl=" + ttl + "s" +
                '}';
    }
}
