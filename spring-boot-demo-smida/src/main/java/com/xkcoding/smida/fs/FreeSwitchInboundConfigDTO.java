package com.xkcoding.smida.fs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Created by YangYifan on 2021/5/13.
 */
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
public class FreeSwitchInboundConfigDTO {
    private String inboundHost;
    private Integer inboundPort;
    private String inboundPassword;
    private Integer inboundTimeout;
}
