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

@Configuration
public class CacheConfiguration {

    public static final String REQUEST_SCOPED_CACHE_MANAGER = "requestScopeCacheManager";
    public static final String LOCAL_CACHE_MANAGER = "localCacheManager";

    public static final String ORGANISATION_CACHE = "organisationCache";
    public static final String JUDICIAL_USER_CACHE = "judicialUserCache";
    public static final String SYS_USER_CACHE = "systemUserCache";

    public static final int SYSTEM_USER_CACHE_EXPIRY = 120;

    @Primary
    @Bean(autowireCandidate = false)
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public CacheManager requestScopeCacheManager() {
        final SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
        simpleCacheManager.setCaches(List.of(
            new ConcurrentMapCache(ORGANISATION_CACHE),
            new ConcurrentMapCache(JUDICIAL_USER_CACHE)));
        simpleCacheManager.initializeCaches();
        return simpleCacheManager;
    }

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

}
