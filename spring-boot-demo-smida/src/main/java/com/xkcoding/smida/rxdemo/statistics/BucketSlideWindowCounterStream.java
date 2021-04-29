package com.xkcoding.smida.rxdemo.statistics;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

/**
 * @author Created by YangYifan on 2021/4/26.
 */
public abstract class BucketSlideWindowCounterStream<Event extends HermesEvent, Bucket extends StatisticMetrics, Output extends StatisticMetrics> extends BucketCounterStream<Event, Bucket, Output> {
    private Observable<Output> sourceStream;

    protected BucketSlideWindowCounterStream(HermesEventStream<Event> stream,
                                             final int numBuckets,
                                             int bucketSizeInMs,
                                             String commandKey,
                                             final BiFunction<Bucket, Event, Bucket> appendRawEventToBucket,
                                             final BiFunction<Output, Bucket, Output> reduceBucket) {
        super(stream, numBuckets, bucketSizeInMs, commandKey, appendRawEventToBucket);
        Function<Observable<Bucket>, ObservableSource<Output>> reduceWindowToSummary =
            new Function<Observable<Bucket>, ObservableSource<Output>>() {
                @Override
                public ObservableSource<Output> apply(Observable<Bucket> window) throws Exception {
                    return window.scan(getEmptyMetricsOutputData(commandKey), reduceBucket).skip(numBuckets);
                }
            };
        this.sourceStream = bucketedStream
            //将桶的事件流，每numBucket一组，聚合在一起，并每次跳过一个之后再聚合
            //如事件流 1,2,3,4,5...... numBuckets 为3 ,则聚合后为 1,2,3  2,3,4  3,4,5 ......
            .window(numBuckets, 1)
            //将聚合后的Bucket流计算为最终的统计结果
            .flatMap(reduceWindowToSummary)
            //可被多个observer订阅，为日后拓展功能做准备
            .share();
    }

    @Override
    public Observable<Output> observe() {
        return sourceStream;
    }
}
