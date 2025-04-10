package uk.gov.hmcts.reform.fpl.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.controllers.cafcass.CafcassCasesController.CAFCASS_API_SEARCH_WINDOW_IN_MINUTE;

@Configuration
public class CacheConfiguration {

    public static final String REQUEST_SCOPED_CACHE_MANAGER = "requestScopeCacheManager";
    public static final String LOCAL_CACHE_MANAGER = "localCacheManager";
    public static final String CAFCASS_API_IDAM_CACHE_MANAGER = "cafcassApiIdamCacheManager";

    public static final String ORGANISATION_CACHE = "organisationCache";
    public static final String SYS_USER_CACHE = "systemUserCache";
    public static final String CAFCASS_API_IDAM_CACHE = "cafcassApiIdamCache";

    public static final int SYSTEM_USER_CACHE_EXPIRY = 120;

    @Primary // primary for request scope
    @Bean(autowireCandidate = false)
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public CacheManager requestScopeCacheManager() {
        final SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
        simpleCacheManager.setCaches(List.of(
            new ConcurrentMapCache(ORGANISATION_CACHE)));
        simpleCacheManager.initializeCaches();
        return simpleCacheManager;
    }

    @Primary // primary for application scope
    @Bean
    @Scope(value = WebApplicationContext.SCOPE_APPLICATION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public CacheManager localCacheManager() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager(SYS_USER_CACHE);
        caffeineCacheManager.setCaffeine(Caffeine.newBuilder()
            .initialCapacity(10)
            .maximumSize(100)
            .expireAfterWrite(Duration.ofMinutes(SYSTEM_USER_CACHE_EXPIRY)));
        return caffeineCacheManager;
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_APPLICATION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public CacheManager cafcassApiIdamCacheManager() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager(CAFCASS_API_IDAM_CACHE);
        caffeineCacheManager.setCaffeine(Caffeine.newBuilder()
            .initialCapacity(10)
            .maximumSize(100)
            .expireAfterWrite(Duration.ofMinutes(CAFCASS_API_SEARCH_WINDOW_IN_MINUTE)));
        return caffeineCacheManager;
    }
}
