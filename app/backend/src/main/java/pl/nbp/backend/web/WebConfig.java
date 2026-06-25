package pl.nbp.backend.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Web layer configuration: SSE thread pool and other web beans.
 */
@Configuration
public class WebConfig {

    /**
     * Thread pool executor for SSE streaming tasks.
     *
     * <p>Named {@code sseTaskExecutor} to avoid ambiguity with Spring Boot's
     * auto-configured {@code taskExecutor} bean from {@code TaskExecutionAutoConfiguration}.
     *
     * @return the configured task executor
     */
    @Bean
    public TaskExecutor sseTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("sse-");
        executor.initialize();
        return executor;
    }
}
