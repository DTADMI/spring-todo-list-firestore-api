package ca.dtadmi.todolist.config;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateConfiguration {

    @Bean
    public RateLimiter rateLimiter(){
        return RateLimiter.create(100.0);
    }
}
