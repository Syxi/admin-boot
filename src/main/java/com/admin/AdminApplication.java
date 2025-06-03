package com.admin;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@EnableCaching
@SpringBootApplication(scanBasePackages = {"com.admin"})
@MapperScan(basePackages = {"com.admin.module.*.mapper"})
public class AdminApplication {

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext context = SpringApplication.run(AdminApplication.class, args);
        ConfigurableEnvironment environment = context.getEnvironment();
        // 将 env 中所有属性放到 redis中
        String contextPath = environment.getProperty("server.servlet.context.path");
        if (StringUtils.isEmpty(contextPath)) {
            contextPath = "";
        }
        // 本机ip地址
        String ip = InetAddress.getLocalHost().getHostAddress();
        // 端口号
        String port = environment.getProperty("server.port");
        log.info("\n#####################################################################################\n" +
                "#\tYanApplication is running! Access URLs:\n" +
                "#\tLocal在线接口文档： \thttp://" + ip + ":" + port +  "/doc.html" + "\n");
    }

}
