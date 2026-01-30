package com.uth.confms.email.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration cho async email sending
 */
@Configuration
@EnableAsync
public class AsyncEmailConfig implements AsyncConfigurer {

    @Value("${app.email.async.core-pool-size:5}")
    private int corePoolSize;

    @Value("${app.email.async.max-pool-size:10}")
    private int maxPoolSize;

    @Value("${app.email.async.queue-capacity:100}")
    private int queueCapacity;

    @Value("${app.email.async.thread-name-prefix:email-async-}")
    private String threadNamePrefix;

    @Override
    @Bean(name = "emailTaskExecutor")
    // Cấu hình Executor cho việc gửi email bất đồng bộ
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
