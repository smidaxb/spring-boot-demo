package com.xkcoding.smida.testDemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Created by YangYifan on 2020/8/11.
 */
@Service
public class XXXService {
    @Autowired
    private XXXDao xxxDao;

    public int getCount() {
        return xxxDao.getCountSql();
    }
}
