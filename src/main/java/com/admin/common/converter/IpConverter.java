package com.admin.common.converter;

import ch.qos.logback.core.PropertyDefinerBase;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 建类说明： ip转换器
 *
 * logback-spring.xml中的ip自动解析
 *
 */
public class IpConverter extends PropertyDefinerBase {
    @Override
    public String getPropertyValue() {
        String key = "ip";
        String ip = System.getProperty(key);
        // JVM参数优先级最高， 如果未指定JVM参数，则由程序获取ip地址
        if (StringUtils.isBlank(ip)) {
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                ip = "UnknownHost";
            }
        }
        return ip;
    }
}
