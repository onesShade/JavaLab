package javalab.config;

import javalab.model.Author;
import javalab.model.Book;
import javalab.utility.InMemoryCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public InMemoryCache<Long, Book> bookCache() {
        return new InMemoryCache<>(16);
    }

    @Bean
    public InMemoryCache<Long, Author> authorCache() {
        return new InMemoryCache<>(16);
    }
}