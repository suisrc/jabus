package com.myfmes.github.fwk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot Starter
 * @see EnableScheduling
 * @author Frank Zhang
 */
@EnableAutoConfiguration
@SpringBootApplication(scanBasePackages = {"com.myfmes.github", "com.suisrc.kratos"})
public class Application {

    public static void main(String[] args) {
        try {
            SpringApplication.run(Application.class, args);
        } catch (Throwable e) { // 解决系统级错误异常， 无法显示异常
            e.printStackTrace(System.err);
        }
    }
}
