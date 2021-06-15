package com.xkcoding.smida.sunlands.fs;

import com.alibaba.fastjson.JSON;
import org.freeswitch.esl.client.inbound.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//@Component
//@Order(2)
public class FreeswtichInboundClient implements CommandLineRunner, InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(FreeswtichInboundClient.class);
    public static Map<String, Client> fsClientMap = new ConcurrentHashMap<>(2);

//    public static Client client;

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
//        client = new Client();
//        client.connect(fsHost, fsPort, fsPassWord, fsTimeout);
//        client.setEventSubscriptions("plain", "all");
//        //监听事件
//        client.addEventListener(myEslEventListener);

        FreeSwitchInboundConfigDTO inboundConfig = JSON.parseObject("{\"inboundHost\":\"172.16.140.71\",\"inboundPassword\":\"ClueCon\",\"inboundPort\":8021,\"inboundTimeout\":60}", FreeSwitchInboundConfigDTO.class);
        Client fsClient = new Client();
        fsClient.connect(inboundConfig.getInboundHost(), inboundConfig.getInboundPort(), inboundConfig.getInboundPassword(), inboundConfig.getInboundTimeout());
        fsClientMap.put("fsNode1", fsClient);
        logger.info("FreeSwitchInboundClient1 初始化SUCCESS");
        fsClient.addEventListener(myEslEventListener);
        fsClient.setEventSubscriptions("plain", "all");

        FreeSwitchInboundConfigDTO inboundConfig2 = JSON.parseObject("{\"inboundHost\":\"10.247.100.14\",\"inboundPassword\":\"ClueCon\",\"inboundPort\":8021,\"inboundTimeout\":60}", FreeSwitchInboundConfigDTO.class);
        Client fsClient2 = new Client();
        fsClient2.connect(inboundConfig.getInboundHost(), inboundConfig.getInboundPort(), inboundConfig.getInboundPassword(), inboundConfig.getInboundTimeout());
        fsClientMap.put("fsNode2", fsClient);
        logger.info("FreeSwitchInboundClient2 初始化SUCCESS");
        fsClient2.addEventListener(myEslEventListener);
        fsClient2.setEventSubscriptions("plain", "all");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        fsHost = freeSwitchConfig.getInboundHost();
        fsPort = freeSwitchConfig.getInboundPort();
        fsPassWord = freeSwitchConfig.getInboundPassword();
        fsTimeout = freeSwitchConfig.getInboundTimeout();
    }

    public static void closeSingle(String key) {
        fsClientMap.get(key).close();
        fsClientMap.remove(key);
    }

    /**
     * freeSwitch.inbound-host=172.16.140.71
     * freeSwitch.inbound-port=8021
     * freeSwitch.inbound-password=ClueCon
     * freeSwitch.inbound-timeout=60
     * <p>
     * freeSwitch.inbound-host=10.247.100.14
     * freeSwitch.inbound-port=8021
     * freeSwitch.inbound-password=ClueCon
     * freeSwitch.inbound-timeout=60
     * <p>
     * {"inboundHost":"172.16.140.71","inboundPassword":"ClueCon","inboundPort":8021,"inboundTimeout":60}
     * {"inboundHost":"10.247.100.14","inboundPassword":"ClueCon","inboundPort":8021,"inboundTimeout":60}
     *
     * @param args
     */
    public static void main(String[] args) {
        FreeSwitchInboundConfigDTO configDTO = FreeSwitchInboundConfigDTO.builder()
            .inboundHost("172.16.140.71")
            .inboundPassword("ClueCon")
            .inboundPort(8021)
            .inboundTimeout(60)
            .build();

        FreeSwitchInboundConfigDTO configDTO2 = FreeSwitchInboundConfigDTO.builder()
            .inboundHost("10.247.100.14")
            .inboundPassword("ClueCon")
            .inboundPort(8021)
            .inboundTimeout(60)
            .build();

        System.out.println(JSON.toJSONString(configDTO));
        System.out.println(JSON.toJSONString(configDTO2));

        Map map = new HashMap();
        map.put("a", "a");
        map.put("b", "a");
        map.put("c", "a");
        System.out.println(null!=map.get("d"));
        map.remove("d");
        System.out.println(map);
    }
}
