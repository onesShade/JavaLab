package javalab.service;

import java.util.concurrent.atomic.AtomicLong;
import javalab.logger.NoLogging;
import org.springframework.stereotype.Service;

@Service
public class VisitCounterService {
    private final AtomicLong counter = new AtomicLong(0);

    @NoLogging
    public void increment() {
        counter.incrementAndGet();
    }

    public Long getCount() {
        return counter.get();
    }
}
