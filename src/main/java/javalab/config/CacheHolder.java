package javalab.config;

import javalab.model.Author;
import javalab.model.Book;
import javalab.model.Log;
import javalab.utility.Cache;
import lombok.Getter;

@Getter
public class CacheHolder {
    private final Cache<Long, Author> authorCache;
    private final Cache<Long, Book> bookCache;
    private final Cache<Long, Log> logFileCache;

    CacheHolder() {
        this.authorCache = new Cache<>(16);
        this.bookCache = new Cache<>(16);
        this.logFileCache = new Cache<>(1);
    }
}