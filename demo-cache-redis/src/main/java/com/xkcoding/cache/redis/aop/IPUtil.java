package com.xkcoding.cache.redis.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by yusong on 2017/7/25.
 * 获取IP地址工具类
 */
public class IPUtil {
    private static final Logger logger = LoggerFactory.getLogger(IPUtil.class);

    //默认本机IP地址
    private static String DEFAULT_LOCAL_HOST = "127.0.0.1";

    /*
    *获取本机IP地址
    */
    public static String getLocalHost(){
        String host = null;
        try {
            if (isWindowsOS()){
                host = InetAddress.getLocalHost().getHostAddress();
            }else{
                host = getLinuxLocalIp();
            }
        } catch (Exception e) {
            logger.warn("getLocalHost|获取本地ip地址出错|",e);
            host = DEFAULT_LOCAL_HOST;
        }
        return host;
    }


    /**
     * 获取Linux下的IP地址
     *
     * @return IP地址
     * @throws SocketException
     */
    private static String getLinuxLocalIp() throws SocketException {
        String ip = "";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                String name = intf.getName();
                if (!name.contains("docker") && !name.contains("lo")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            String ipaddress = inetAddress.getHostAddress().toString();
                            if (!ipaddress.contains("::") && !ipaddress.contains("0:0:") && !ipaddress.contains("fe80")) {
                                ip = ipaddress;
                                logger.info(ipaddress);
                            }
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            ip = DEFAULT_LOCAL_HOST;
            logger.info("getLinuxLocalIp|获取Linux Ip异常",ex);
        }
        logger.info("IP:{}",ip);
        return ip;
    }

    /**
     * 判断操作系统是否是Windows
     *
     * @return
     */
    public static Boolean isWindowsOS() {
        boolean isWindowsOS = false;
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().indexOf("windows") > -1) {
            isWindowsOS = true;
        }
        return isWindowsOS;
    }

    private IPUtil() {
    }
}
