package com.xkcoding.smida.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Set;

/**
 * 获取IP地址工具类
 */
public class IPUtil {
    private static final Logger logger = LoggerFactory.getLogger(IPUtil.class);

    //默认本机IP地址
    private static String DEFAULT_LOCAL_HOST = "127.0.0.1";

    //服务器IP
    private static String ip;
    private static String port;
    private static String ip_port;

    public static void main(String[] args) {
        System.out.println("sfasdfsaf" + null);
    }

    /**
     * 获取本机IP地址
     */
    public static String getLocalHost() {
        if (ip != null) {
            return ip;
        }
        String host = null;
        try {
            if (isWindowsOS()) {
                host = InetAddress.getLocalHost().getHostAddress();
            } else {
                host = getLinuxLocalIp();
            }
            ip = host;
        } catch (Exception ignore) {
            logger.warn("getLocalHost|无法获取服务器IP地址|", ignore);
            host = DEFAULT_LOCAL_HOST;
            return host;
        }
        return ip;
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
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                String name = intf.getName();
                if (!name.contains("docker") && !name.contains("lo")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
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
            logger.info("getLinuxLocalIp|获取Linux Ip异常", ex);
        }
        logger.info("IP:{}", ip);
        return ip;
    }


    /**
     * 获取当前机器端口号
     */
    public static String getLocalPort() {
        if (port != null) {
            return port;
        }
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectName> objectNames = mBeanServer.queryNames(new ObjectName("*:type=Connector,*"), null);
            if (objectNames == null || objectNames.size() <= 0) {
                logger.warn("Cannot get the names of MBeans controlled by the MBean server.");
            }
            for (ObjectName objectName : objectNames) {
                String protocol = String.valueOf(mBeanServer.getAttribute(objectName, "protocol"));
                String iport = String.valueOf(mBeanServer.getAttribute(objectName, "port"));
                // windows下属性名称为HTTP/1.1, linux下为org.apache.coyote.http11.Http11NioProtocol
                if (protocol.equals("HTTP/1.1") || protocol.equals("org.apache.coyote.http11.Http11NioProtocol")) {
                    port = iport;
                    return port;
                }
            }
        } catch (Exception ignore) {
            logger.warn("获取端口异常", ignore);
            port = "";
        }
        return port;
    }

    public static String getIpAndPort() {
        if (ip_port != null) {
            return ip_port;
        }
        ip_port = getLocalHost() + ":" + getLocalPort();
        return ip_port;
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
        throw new UnsupportedOperationException("It's prohibited to create instances of the class.");
    }
}
