package com.example.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name = "taskExecutor")
    public AsyncTaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("async-");
        executor.setVirtualThreads(true);
        return executor;
    }

    @Bean(name = "orderProcessingExecutor")
    public AsyncTaskExecutor orderProcessingExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("order-proc-");
        executor.setVirtualThreads(true);
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return taskExecutor();
    }

    // Only applies to void-returning @Async methods — CompletableFuture-returning ones
    // (all of ours, currently) surface exceptions through the future instead, wrapped
    // in a CompletionException when you call .join()/.get(). Kept here for any future
    // fire-and-forget void @Async method, so failures aren't swallowed silently.
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> log.error("Uncaught async error in {}", method.getName(), ex);
    }
}
