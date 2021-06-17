package com.xkcoding.smida.utils.distributedID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 雪花算法生成唯一ID
 * 0 - 41位时间戳 - 10位机器标识 - 12位序列号
 * <p>
 * 其他：
 * 百度基于雪花算法的UID_Generator： https://github.com/baidu/uid-generator/blob/master/README.zh_cn.md
 * 美团leaf(用zk确保workId，并做系统时间回退校验)：https://tech.meituan.com/2017/04/21/mt-leaf.html
 **/
public class SnowflakeIdGenerator {
    private static Logger logger = LoggerFactory.getLogger(SnowflakeIdGenerator.class);
    /**
     * 起始的时间戳：2019-11-21 00:00:00
     */
    private final static long START_STMP = 1574265600000L;

    /**
     * 序列号占用的位数 12
     **/
    private final static long SEQUENCE_BIT = 12;
    /**
     * 机器标识占用的位数 10
     **/
    private final static long MACHINE_BIT = 10;

    /**
     * 每一部分的最大值（这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数）
     * 机器标识最大值 1023
     * 序列号最大值 4095
     */
    private final static long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);

    /**
     * 每一部分向左的位移
     */
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long TIMESTMP_LEFT = SEQUENCE_BIT + MACHINE_BIT;

    /**
     * 机器标识
     **/
    private long machineId;
    /**
     * 序列号
     **/
    private long sequence = 0L;
    /**
     * 上一次时间戳
     **/
    private long lastStmp = -1L;

    /**
     * 用于系统时钟回退
     **/
    private long runRandomTimestamp;
    private int lastRandomValue;

    public static void main(String[] args) {
        SnowflakeIdGenerator m = new SnowflakeIdGenerator(1);
        System.out.println(m.nextId());
        System.out.println(m.nextId());
        System.out.println(m.nextId());
        System.out.println(m.nextId());
        System.out.println(m.nextId());
        System.out.println(m.nextId());
        System.out.println(m.nextId());
        System.out.println(m.nextId());
        System.out.println(m.nextId());
        System.out.println(m.nextId());
    }

    public SnowflakeIdGenerator(long machineId) {
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        this.machineId = machineId;
    }

    /**
     * 产生下一个ID
     *
     * @return
     */
    public synchronized long nextId() {
        long currStmp = getNewstmp();

        if (currStmp == lastStmp) {
            //相同毫秒内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                currStmp = getNextMill();
            }
        } else {
            //不同毫秒内，序列号置为0
            sequence = 0L;
        }

        lastStmp = currStmp;

        return (currStmp - START_STMP) << TIMESTMP_LEFT
            | machineId << MACHINE_LEFT
            | sequence;
    }

    private long getNextMill() {
        long mill = getNewstmp();
        while (mill <= lastStmp) {
            mill = getNewstmp();
        }
        return mill;
    }

    private long getNewstmp() {
        long currStmp = System.currentTimeMillis();
        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过，此时返回线程安全的随机数
        if (currStmp < lastStmp) {
            logger.error("Clock moved backwards,currStmp:{},lastStmp:{}", currStmp, lastStmp);
            if (runRandomTimestamp != currStmp) {
                lastRandomValue = ThreadLocalRandom.current().nextInt();
                runRandomTimestamp = currStmp;
            }
            return lastRandomValue;
        }
        return currStmp;
    }
}
