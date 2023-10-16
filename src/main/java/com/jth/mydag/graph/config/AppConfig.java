package com.jth.mydag.graph.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author jiatihui
 * @Description: 线程池配置实例.
 */
@Configuration
public class AppConfig {

    @Bean
    public ExecutorService executorService() {
        return new ThreadPoolExecutor(
                64,
                128,
                500L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
    }
}
