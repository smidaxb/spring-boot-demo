package com.xkcoding.smida.fs;

import org.freeswitch.esl.client.inbound.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
//@Component
////@Order(2)
public class FreeswtichInboundClient implements CommandLineRunner, InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(FreeswtichInboundClient.class);

    public static Client client;

    private String fsHost;
    private Integer fsPort;
    private String fsPassWord;
    private Integer fsTimeout;

    @Autowired
    private MyEslEventListener myEslEventListener;
    @Autowired
    private FreeSwitchConfig freeSwitchConfig;

    @Override
    public void run(String... strings) throws Exception {
        client = new Client();
        client.connect(fsHost, fsPort, fsPassWord, fsTimeout);
        client.setEventSubscriptions("plain", "HEARTBEAT");
        //监听事件
        client.addEventListener(myEslEventListener);
        logger.info("FreeSwitchInboundClient 初始化SUCCESS");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        fsHost = freeSwitchConfig.getInboundHost();
        fsPort = freeSwitchConfig.getInboundPort();
        fsPassWord = freeSwitchConfig.getInboundPassword();
        fsTimeout = freeSwitchConfig.getInboundTimeout();
    }
}
