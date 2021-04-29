package com.xkcoding.smida.fs;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

//@Configuration("freeSwitchConfig")
@Slf4j
@Data
//@EnableConfigurationProperties(FreeSwitchConfig.class)
//@ConfigurationProperties("freeswitch")
public class FreeSwitchConfig implements InitializingBean {
    private String inboundHost;
    private Integer inboundPort;
    private String inboundPassword;
    private Integer inboundTimeout;

    private String callCommand;
    private String callGateway;
    private String callGatewayAddr;

    private String recordFilePath;
    private String recordPrefix;

    private Integer outboundBdPort;
    private Integer outboundRcrPort;
    private Integer outboundTtsPort;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("freeSwitchConfig lod SUCCESS");
    }
}
