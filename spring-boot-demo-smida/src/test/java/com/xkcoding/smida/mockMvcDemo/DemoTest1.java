package com.xkcoding.smida.mockMvcDemo;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * @author Created by YangYifan on 2020/8/11.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc //该注解将会自动配置mockMvc的单元测试
public class DemoTest1 {
    @Autowired
    public MockMvc mockMvc;
    @Autowired
    private ApplicationContext applicationContext;
    @MockBean
    private XXXDao xxxDao;

    @Test
    public void demoTest1() {
//        mockMvc = MockMvcBuilders.standaloneSetup(XXXController.class).build();
        Mockito.when(xxxDao.getCountSql()).thenReturn(1);

        //MockBean mock的对象会自动注入到上下文中
        XXXDao xxxDao1 = applicationContext.getBean(XXXDao.class);
        System.out.println(xxxDao1.getCountSql());
        System.out.println(xxxDao.getCountSql());
    }

    @Test
    public void demoTest2() {
        xxxDao = Mockito.mock(XXXDao.class);
        Mockito.when(xxxDao.getCountSql()).thenReturn(2);
        System.out.println(xxxDao.getCountSql());
        Mockito.verify(xxxDao).getCountSql();
    }

    @Test
    public void demoTest3() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/url/u1"))
            .andExpect(MockMvcResultMatchers.view().name("user/view"))
            .andExpect(MockMvcResultMatchers.model().attributeExists("username"))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
            .andDo(MockMvcResultHandlers.print())
            .andReturn();
        Assert.assertNotNull(result.getModelAndView().getModel().get("username"));
    }

}
