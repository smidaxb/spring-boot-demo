package com.xkcoding.smida.rxdemo.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mrcp统计指标类
 * @author Created by YangYifan on 2021/4/26.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MrcpMetrics implements StatisticMetrics {
    private int mrcpReqCount;
    private int mrcpOvertimeCount;
    private int mrcpErrorCount;
    private int mrcpAsrResCount;

    @Override
    public StatisticMetrics toSumMetrics(StatisticMetrics other) {
        MrcpMetrics metrics = (MrcpMetrics) other;
        this.setMrcpReqCount(mrcpReqCount + metrics.getMrcpReqCount());
        this.setMrcpOvertimeCount(mrcpOvertimeCount + metrics.getMrcpOvertimeCount());
        this.setMrcpErrorCount(mrcpErrorCount + metrics.getMrcpErrorCount());
        this.setMrcpAsrResCount(mrcpAsrResCount + metrics.getMrcpAsrResCount());
        return this;
    }

    public static StatisticMetrics getEmpty() {
        return new MrcpMetrics(0, 0, 0, 0);
    }
}
