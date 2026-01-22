package za.co.psybergate.chatterbox.infrastructure.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import za.co.psybergate.chatterbox.infrastructure.common.config.properties.concurrency.ExecutorProperties;

import java.util.concurrent.ThreadPoolExecutor;

@SuppressWarnings("DuplicatedCode")
@Configuration
@RequiredArgsConstructor
public class ThreadConfig {

    private final ExecutorProperties polledEventExecutorProperties;

    private final ExecutorProperties webhookEventExecutorProperties;

    @Bean("polledEventExecutor")
    public ThreadPoolTaskExecutor polledEventExecutor() {
        int threadCount = polledEventExecutorProperties.getThreadCount();
        int maxPoolSize = polledEventExecutorProperties.getMaxPoolSize();
        int queueCapacity = polledEventExecutorProperties.getQueueCapacity();
        String threadNamePrefix = polledEventExecutorProperties.getThreadNamePrefix();
        ThreadPoolTaskExecutor executor = createExecutor(threadCount, maxPoolSize, queueCapacity, threadNamePrefix);
        executor.initialize();
        return executor;
    }

    @Bean("webhookEventExecutor")
    public ThreadPoolTaskExecutor webhookEventExecutor() {
        int threadCount = webhookEventExecutorProperties.getThreadCount();
        int maxPoolSize = webhookEventExecutorProperties.getMaxPoolSize();
        int queueCapacity = webhookEventExecutorProperties.getQueueCapacity();
        String threadNamePrefix = webhookEventExecutorProperties.getThreadNamePrefix();
        ThreadPoolTaskExecutor executor = createExecutor(threadCount, maxPoolSize, queueCapacity, threadNamePrefix);
        executor.initialize();
        return executor;
    }

    private ThreadPoolTaskExecutor createExecutor(int threadCount, int maxPoolSize, int queueCapacity, String threadNamePrefix) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadCount);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        return executor;
    }

}
