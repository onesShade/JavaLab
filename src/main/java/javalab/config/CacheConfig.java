package javalab.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public CacheHolder cacheHolder() {
        return new CacheHolder();
    }
}