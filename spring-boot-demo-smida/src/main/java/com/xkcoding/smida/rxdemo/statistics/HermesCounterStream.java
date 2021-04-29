package com.xkcoding.smida.rxdemo.statistics;

import io.reactivex.functions.BiFunction;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Created by YangYifan on 2021/4/26.
 */
public class HermesCounterStream extends BucketSlideWindowCounterStream<StatisticMetrics, StatisticMetrics, StatisticMetrics> {

    private static final ConcurrentMap<String, HermesCounterStream> streams = new ConcurrentHashMap<>();

    public static final String FREE_SWITCH_METRICS_TYPE = "FREE_SWITCH";
    public static final String MRCP_METRICS_TYPE = "MRCP";
    private static final BiFunction<StatisticMetrics, StatisticMetrics, StatisticMetrics> mergeMetricsData =
        new BiFunction<StatisticMetrics, StatisticMetrics, StatisticMetrics>() {
            @Override
            public StatisticMetrics apply(StatisticMetrics output, StatisticMetrics bucket) throws Exception {
                return output.toSumMetrics(bucket);
            }
        };

    protected HermesCounterStream(String commandKey, int numBuckets, int bucketSizeInMs) {
        super(HermesMetricsEventStream.getInstance(commandKey), numBuckets, bucketSizeInMs, commandKey, mergeMetricsData, mergeMetricsData);
    }

    /**
     * @param commandKey     统计类型Key 每个FreeSwitch/mrcp节点均不相同
     * @param numBuckets     窗口中包含桶的个数
     * @param bucketTimeInMs 每个桶的时间宽度
     * @return
     */
    public static HermesCounterStream getInstance(String commandKey, int numBuckets, int bucketTimeInMs) {
        HermesCounterStream initialStream = streams.get(commandKey);
        if (initialStream != null) {
            return initialStream;
        } else {
            synchronized (HermesCounterStream.class) {
                HermesCounterStream existingStream = streams.get(commandKey);
                if (existingStream == null) {
                    HermesCounterStream newStream =
                        new HermesCounterStream(commandKey, numBuckets, bucketTimeInMs);
                    streams.putIfAbsent(commandKey, newStream);
                    return newStream;
                } else {
                    return existingStream;
                }
            }
        }
    }

    @Override
    StatisticMetrics getEmptyMetricsBucketData(String commandKey) {
        return getEmptyMetricsOutputData(commandKey);
    }

    @Override
    StatisticMetrics getEmptyMetricsOutputData(String commandKey) {
        switch (commandKey) {
            case FREE_SWITCH_METRICS_TYPE:
                return FreeSwitchMetrics.getEmpty();
            case MRCP_METRICS_TYPE:
                return MrcpMetrics.getEmpty();
            default:
                return null;
        }
    }

}
