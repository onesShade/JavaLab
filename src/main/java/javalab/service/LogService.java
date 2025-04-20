package javalab.service;

import java.util.concurrent.atomic.AtomicLong;
import javalab.config.CacheHolder;
import javalab.logger.NoLogging;
import javalab.model.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogService {
    private static final String NO_SUCH_LOG = "No such log id exists in cache";
    private final CacheHolder cacheHolder;
    private final AtomicLong idCounter = new AtomicLong(1);
    private final AsyncLogService asyncLogService;

    @Autowired
    public LogService(CacheHolder cacheHolder, AsyncLogService asyncLogService) {
        this.cacheHolder = cacheHolder;
        this.asyncLogService = asyncLogService;
    }

    public Log generateLogs(String date) {
        Long id = idCounter.getAndIncrement();
        Log log = new Log(id, Log.Status.IN_PROGRESS, date);
        cacheHolder.getLogFileCache().put(id, log);
        asyncLogService.createLogs(id, date);
        return log;
    }

    public String getLogStatus(Long id) {
        if (!cacheHolder.getLogFileCache().containsKey(id)) {
            return NO_SUCH_LOG;
        }
        return cacheHolder.getLogFileCache().get(id).getStatus().name();
    }

    public String getLogDate(Long id) {
        if (!cacheHolder.getLogFileCache().containsKey(id)) {
            return NO_SUCH_LOG;
        }
        return cacheHolder.getLogFileCache().get(id).getDate();
    }

    @NoLogging
    public String getLogBody(Long id) {
        if (!cacheHolder.getLogFileCache().containsKey(id)) {
            return NO_SUCH_LOG;
        }

        if (cacheHolder.getLogFileCache().get(id).getStatus() == Log.Status.IN_PROGRESS) {
            return "Log file still in progress.";
        }
        if (cacheHolder.getLogFileCache().get(id).getStatus() == Log.Status.FAILURE) {
            return "Failure to generate log.";
        }
        return cacheHolder.getLogFileCache().get(id).getBody();
    }
}