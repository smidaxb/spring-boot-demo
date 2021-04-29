package com.xkcoding.smida.rxdemo.statistics;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 统计指标的事件流类，用于写入事件到统计流中
 * @author Created by YangYifan on 2021/4/26.
 */
public class HermesMetricsEventStream implements HermesEventStream<StatisticMetrics> {
    private final String commandKey;
    private final Subject<StatisticMetrics> writeOnlySubject;
    private final Observable<StatisticMetrics> readOnlyStream;
    private static final ConcurrentMap<String, HermesMetricsEventStream> streams = new ConcurrentHashMap<>();

    public HermesMetricsEventStream(String commandKey) {
        this.commandKey = commandKey;
        this.writeOnlySubject = PublishSubject.<StatisticMetrics>create().toSerialized();
        this.readOnlyStream = writeOnlySubject.share();
    }

    public static HermesMetricsEventStream getInstance(String commandKey) {
        //若对应的 CommandKey 的事件流已创建就从缓存中取出，否则就新创建并缓存起来，保证每个 CommandKey 只有一个实例
        HermesMetricsEventStream initialStream = streams.get(commandKey);
        if (initialStream != null) {
            return initialStream;
        } else {
            synchronized (HermesMetricsEventStream.class) {
                HermesMetricsEventStream existingStream = streams.get(commandKey);
                if (existingStream == null) {
                    HermesMetricsEventStream newStream = new HermesMetricsEventStream(commandKey);
                    streams.putIfAbsent(commandKey, newStream);
                    return newStream;
                } else {
                    return existingStream;
                }
            }
        }
    }

    public void write(StatisticMetrics metricsEvent) {
        writeOnlySubject.onNext(metricsEvent);
    }

    @Override
    public Observable<StatisticMetrics> observe() {
        return readOnlyStream;
    }
}
