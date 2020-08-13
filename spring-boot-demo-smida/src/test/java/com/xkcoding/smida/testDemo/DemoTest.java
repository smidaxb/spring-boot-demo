package com.xkcoding.smida.testDemo;

import com.xkcoding.smida.testDemo.XXXDao;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Created by YangYifan on 2020/8/11.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class DemoTest {


    @Test
    public void mockClassTest() {
        //mock对象
        XXXDao xxxDaoMock1 = Mockito.mock(XXXDao.class);
        //指定mock对象的行为
        Mockito.when(xxxDaoMock1.getCountSql()).thenReturn(0);
        //调用mock对象
        int count = xxxDaoMock1.getCountSql();
        Assert.assertEquals(0, count);
        //验证localMockRepository的方法被调用
        Mockito.verify(xxxDaoMock1).getCountSql();
    }

    @Mock
    private XXXDao xxxDaoMock2;
    @Test
    public void mockClassWithMockAnnotation() {
        MockitoAnnotations.initMocks(this);
        //指定mock对象的行为
        Mockito.when(xxxDaoMock2.getCountSql()).thenReturn(0);
        //调用mock对象
        int count = xxxDaoMock2.getCountSql();
        Assert.assertEquals(0, count);
        //验证localMockRepository的方法被调用
        Mockito.verify(xxxDaoMock2).getCountSql();
    }

    @MockBean
    private XXXDao xxxDao;
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void mockClassWithMockBeanAnnotation() {
        //MockBean mock的对象会自动注入到上下文中
        XXXDao xxxDaoFromContext = applicationContext.getBean(XXXDao.class);
        //指定mock对象的行为
        Mockito.when(xxxDao.getCountSql()).thenReturn(0);
        //调用mock对象
        int count = xxxDao.getCountSql();
        int countContext = xxxDaoFromContext.getCountSql();
        Assert.assertEquals(countContext, count);
        //验证localMockRepository的方法被调用两次
        Mockito.verify(xxxDao, Mockito.times(2)).getCountSql();
    }

}
