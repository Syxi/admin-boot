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

/**
 * 系统启动类
 * 
 * @author suYan
 */
@Slf4j
@EnableCaching
@SpringBootApplication(scanBasePackages = {"com.admin"})
@MapperScan(basePackages = {"com.admin.module.*.mapper"})
public class AdminApplication {

    public static void main(String[] args) {
        try {
            ConfigurableApplicationContext context = SpringApplication.run(AdminApplication.class, args);
            logApplicationStartup(context);
        } catch (Exception e) {
            log.error("应用启动失败", e);
            System.exit(1);
        }
    }

    /**
     * 输出启动信息
     */
    private static void logApplicationStartup(ConfigurableApplicationContext context) {
        ConfigurableEnvironment environment = context.getEnvironment();
        String contextPath = environment.getProperty("server.servlet.context-path");
        if (StringUtils.isEmpty(contextPath)) {
            contextPath = "";
        }

        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            String port = environment.getProperty("server.port");
            String applicationName = environment.getProperty("spring.application.name", "Admin-Boot");

            log.info("\n" +
                    "========================================================================================================\n" +
                    "\t应用程序 '{}' 运行成功！\n" +
                    "\t访问地址:\n" +
                    "\t\t本地: \t\thttp://localhost:{}{}\n" +
                    "\t\t外部: \t\thttp://{}:{}{}\n" +
                    "\t\tSwagger文档: \thttp://{}:{}{}/doc.html\n" +
                    "========================================================================================================",
                    applicationName,
                    port, contextPath,
                    ip, port, contextPath,
                    ip, port, contextPath
            );
        } catch (UnknownHostException e) {
            log.warn("无法获取主机IP地址", e);
        }
    }
}
