package javalab.config;

import javalab.model.Author;
import javalab.model.Book;
import javalab.utility.Cache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public Cache<Long, Book> bookCache() {
        return new Cache<>(16);
    }

    @Bean
    public Cache<Long, Author> authorCache() {
        return new Cache<>(16);
    }
}