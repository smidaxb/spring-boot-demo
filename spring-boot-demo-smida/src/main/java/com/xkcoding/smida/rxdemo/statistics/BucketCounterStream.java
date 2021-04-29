package com.xkcoding.smida.rxdemo.statistics;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Created by YangYifan on 2021/4/26.
 */
public abstract class BucketCounterStream<Event extends HermesEvent, Bucket, Output> {
    protected final int numBuckets;
    protected final Observable<Bucket> bucketedStream;
    protected final AtomicReference<Subscription> subscription = new AtomicReference((Object) null);
    private final Function<Observable<Event>, ObservableSource<Bucket>> reduceBucketToSummary;

    protected BucketCounterStream(final HermesEventStream<Event> inputEventStream,
                                  int numBuckets, final int bucketSizeInMs, String commandKey,
                                  final BiFunction<Bucket, Event, Bucket> appendRawEventToBucket) {
        this.numBuckets = numBuckets;
        this.reduceBucketToSummary = new Function<Observable<Event>, ObservableSource<Bucket>>() {
            @Override
            public ObservableSource<Bucket> apply(Observable<Event> eventObservable) throws Exception {
                return eventObservable
                    .reduce(BucketCounterStream.this.getEmptyMetricsBucketData(commandKey), appendRawEventToBucket)
                    .toObservable();
            }
        };

        final List<Bucket> emptyEventCountsToStart = new ArrayList();
        for (int i = 0; i < numBuckets; ++i) {
            emptyEventCountsToStart.add(this.getEmptyMetricsBucketData(commandKey));
        }

        this.bucketedStream = Observable.defer(
            //将传入的事件流Event，每bucketSizeInMs聚合为一个桶Bucket
            (Callable<ObservableSource<Bucket>>) () -> inputEventStream
                .observe()
                .window(bucketSizeInMs, TimeUnit.MILLISECONDS)
                .flatMap(BucketCounterStream.this.reduceBucketToSummary)
                //起始的bucketNums个桶数据为初始化的空数据
                .startWith(emptyEventCountsToStart)
        );
    }

    abstract Bucket getEmptyMetricsBucketData(String commandKey);

    abstract Output getEmptyMetricsOutputData(String commandKey);

    public abstract Observable<Output> observe();
}
