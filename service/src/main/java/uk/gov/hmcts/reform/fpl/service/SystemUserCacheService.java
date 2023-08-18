package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;


@Service
public class SystemUserCacheService {

    public SystemUserCacheService() {

    }

    public SystemUserCacheService(String token, LocalDateTime time) {
        this.cachedToken = token;
        this.timeLastCached = time;
    }

    private String cachedToken;
    private LocalDateTime timeLastCached;
    private static final ChronoUnit CACHE_TIME_LIMIT_UNIT = ChronoUnit.HOURS;
    private static final int CACHE_TIME_LIMIT = 2;

    public boolean isCacheValid() {
        return !isEmpty(cachedToken)
            && !isEmpty(timeLastCached)
            && timeLastCached.until(LocalDateTime.now(), CACHE_TIME_LIMIT_UNIT) <= CACHE_TIME_LIMIT;
    }

    public void updateCache(String newToken) {
        this.cachedToken = newToken;
        this.timeLastCached = LocalDateTime.now();
    }

    public String getCachedToken() {
        return this.cachedToken;
    }

    private void setCacheState(String token, LocalDateTime time) {
        this.cachedToken = token;
        this.timeLastCached = time;
    }

}
