package com.xkcoding.smida.utils.distributedID;

import com.xkcoding.smida.utils.IPUtil;

import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * hermesId生成util
 * yyyyMMddHHmmssSSS + 5位10进制IP_CODE(用ip:port计算) + 2^13-序列号
 */
public class SerialNumberUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    //IP地址
    private static String IP_ADDRESS = IPUtil.getLocalHost();
    private static Integer IP_CODE = getSimpleCode(IP_ADDRESS);

    /**
     * 根据环境信息生成序列号
     *
     * @return
     */
    public synchronized static String generateWithInfo() {
        DateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Date now = new Date();
        Integer threadCode = getSimpleCode(Thread.currentThread().getName());
        Integer random = SECURE_RANDOM.nextInt(40000) + IP_CODE + threadCode;
        return sdf.format(now) + String.format("%5d", random);
    }


    private static Integer getSimpleCode(String str) {
        Integer code = str.hashCode();
        if (code < 0) {
            code = -code;
        }
        if (code > 30000) {
            code = code % 30000;
        }
        return code;
    }

    public static void main(String[] args) {
        System.out.println(generateWithInfo());
        System.out.println(generateWithInfo());
        System.out.println(generateWithInfo());
        System.out.println(generateWithInfo());
        System.out.println(generateWithInfo());
    }

}

