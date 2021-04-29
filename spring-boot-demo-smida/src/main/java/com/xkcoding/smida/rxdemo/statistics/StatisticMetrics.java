package com.xkcoding.smida.rxdemo.statistics;

/**
 * @author Created by YangYifan on 2021/4/26.
 */
public interface StatisticMetrics extends HermesEvent{
    /**
     * 将相同类型的指标数据整合
     */
    StatisticMetrics toSumMetrics(StatisticMetrics other);
}
