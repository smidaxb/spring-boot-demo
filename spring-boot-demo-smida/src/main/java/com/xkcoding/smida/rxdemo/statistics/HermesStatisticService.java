package com.xkcoding.smida.rxdemo.statistics;

import com.alibaba.fastjson.JSON;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Created by YangYifan on 2021/4/27.
 */
//@Service
@Slf4j
public class HermesStatisticService implements InitializingBean {
    @Value("${statistics.freeSwitch.bucketSize}")
    private Integer fsBucketSize;
    @Value("${statistics.freeSwitch.bucketTimeInMs}")
    private Integer fsBucketTimeInMs;
    @Value("${statistics.mrcp.bucketSize}")
    private Integer mrcpBucketSize;
    @Value("${statistics.mrcp.bucketTimeInMs}")
    private Integer mrcpBucketTimeInMs;

    private HermesMetricsEventStream fsEventStream;
    private HermesMetricsEventStream mrcpEventStream;
    private static final LinkedBlockingDeque BLOCKING_DEQUE = new LinkedBlockingDeque();
    private static final ThreadPoolExecutor RX_EXECUTOR = new ThreadPoolExecutor(1, 3, 20, TimeUnit.SECONDS, BLOCKING_DEQUE);

    public void sendFreeSwitchEvent(FreeSwitchMetrics metrics) {

    }

    public void sendMrcpEvent(FreeSwitchMetrics metrics) {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initSubscribeFreeSwitchEvent();
        initSubscribeMRCPEvent();
    }

    private void initSubscribeFreeSwitchEvent() {
        fsEventStream = HermesMetricsEventStream.getInstance(HermesCounterStream.FREE_SWITCH_METRICS_TYPE);
        HermesCounterStream fsMetricsStream = HermesCounterStream.getInstance(HermesCounterStream.FREE_SWITCH_METRICS_TYPE, fsBucketSize, fsBucketTimeInMs);
        fsMetricsStream.observe()
            .subscribeOn(Schedulers.from(RX_EXECUTOR))
            .observeOn(Schedulers.from(RX_EXECUTOR))
            .subscribe(
                metrics -> log.info("acceptFreeSwitchMetrics|data:{}", JSON.toJSONString(metrics))
            );
    }

    private void initSubscribeMRCPEvent() {
        mrcpEventStream = HermesMetricsEventStream.getInstance(HermesCounterStream.MRCP_METRICS_TYPE);
        HermesCounterStream mrcpMetricsStream = HermesCounterStream.getInstance(HermesCounterStream.MRCP_METRICS_TYPE, mrcpBucketSize, mrcpBucketTimeInMs);
        mrcpMetricsStream.observe()
            .subscribeOn(Schedulers.from(RX_EXECUTOR))
            .observeOn(Schedulers.from(RX_EXECUTOR))
            .subscribe(
                metrics -> log.info("acceptMrcpMetrics|data:{}", JSON.toJSONString(metrics))
            );
    }
}
