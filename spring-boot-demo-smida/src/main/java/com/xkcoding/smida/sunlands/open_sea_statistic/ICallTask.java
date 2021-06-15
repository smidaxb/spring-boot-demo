package com.xkcoding.smida.sunlands.open_sea_statistic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author Created by YangYifan on 2021/6/11.
 */
@Component
@Slf4j
public class ICallTask {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 * * * * *")
    public void filterPool() {
        System.out.println("===================================");
        String sql = "select id from outcallpool where createTime>'2021-06-11' and (ttsContent like '%股票%' or ttsContent like '%基金%') and deleteFlag=0";
        List<Integer> ids = jdbcTemplate.queryForList(sql, Integer.class);
        if (CollectionUtils.isEmpty(ids)) {
            log.info("filterPool| empty");
            return;
        }
        String updateSql = "update outcallpool set deleteFlag=1 where id=?";
        for (Integer id : ids) {
            jdbcTemplate.update(updateSql, id);
        }
        log.info("filterPool|poolIds:{}", ids);
    }


}
