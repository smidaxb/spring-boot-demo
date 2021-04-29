package com.xkcoding.smida.rxdemo.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FreeSwitch统计指标类
 * @author Created by YangYifan on 2021/4/26.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FreeSwitchMetrics implements StatisticMetrics {
    private int originateReqCount;
    private int originateSucCount;
    private int originateFailCount;
    private int otherReqLoadCount;

    @Override
    public StatisticMetrics toSumMetrics(StatisticMetrics other) {
        FreeSwitchMetrics metrics = (FreeSwitchMetrics) other;
        this.setOriginateReqCount(originateReqCount + metrics.getOriginateReqCount());
        this.setOriginateSucCount(originateSucCount + metrics.getOriginateSucCount());
        this.setOriginateFailCount(originateFailCount + metrics.getOriginateFailCount());
        this.setOtherReqLoadCount(otherReqLoadCount + metrics.getOtherReqLoadCount());
        return this;
    }

    public static StatisticMetrics getEmpty() {
        return new FreeSwitchMetrics(0,0,0,0);
    }
}
