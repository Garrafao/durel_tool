package durel;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


@Configuration
@EnableAsync
public class SpringAsyncConfig {

    @Bean(name = "lightTaskExecutor")
    public ThreadPoolTaskExecutor lightTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100000);
        executor.setThreadNamePrefix("Light-Task-Executor-");
        executor.initialize();
        return executor;
    }
}
