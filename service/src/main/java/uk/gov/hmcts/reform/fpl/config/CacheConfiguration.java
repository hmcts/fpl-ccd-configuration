package uk.gov.hmcts.reform.fpl.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

@Configuration
public class CacheConfiguration {

    public static final String DEFAULT_CACHE = "default";

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public CacheManager requestScopeCacheManager() {
        final SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
        simpleCacheManager.setCaches(List.of(new ConcurrentMapCache(DEFAULT_CACHE)));
        simpleCacheManager.initializeCaches();
        return simpleCacheManager;
    }
}
