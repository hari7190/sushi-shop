package foo.hari.livebarn.sushishop.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;

@Configuration
public class SchedulingConfig {

    @Bean(name = "taskWorkerExecutor")
    public Executor taskWorkerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);       // Exactly 3 threads min
        executor.setMaxPoolSize(3);        // Exactly 3 threads max
        executor.setQueueCapacity(0);      // Don't buffer tasks; execute immediately or skip
        executor.setThreadNamePrefix("chef-");
        executor.initialize();
        return executor;
    }
}