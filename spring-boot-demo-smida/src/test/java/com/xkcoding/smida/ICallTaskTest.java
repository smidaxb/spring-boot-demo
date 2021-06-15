package com.xkcoding.smida;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
@SpringBootTest
@RunWith(SpringRunner.class)
public class ICallTaskTest{
    @Autowired
    private JdbcTemplate jdbcTemplate;

//    @Scheduled(cron = "0 * * * * *")
    @Test
    public void filterPool() {
        System.out.println("===================================");
        String sql = "select id from outcallpool where createTime>'2021-06-11' and (ttsContent like '%股票%' or ttsContent like '%基金%') and deleteFlag=0";;
        List<Integer> ids = jdbcTemplate.queryForList(sql, Integer.class);
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        String updateSql = "update outcallpool set deleteFlag=1 where id=?";
        for (Integer id : ids) {
            jdbcTemplate.update(updateSql, id);
        }
    }


}
