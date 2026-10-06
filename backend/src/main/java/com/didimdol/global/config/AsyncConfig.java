package com.didimdol.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
public class AsyncConfig {

    @Bean(destroyMethod = "close")
    public ExecutorService replyExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
