package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SystemUserCacheServiceTest {

    @Test
    void shouldReturnTrueWhenValid() {
        SystemUserCacheService cache = new SystemUserCacheService("abc", LocalDateTime.now());
        assertThat(cache.isCacheValid()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenInvalid() {
        SystemUserCacheService cache = new SystemUserCacheService("abc", LocalDateTime.now().minusYears(1));
        assertThat(cache.isCacheValid()).isFalse();
    }

    @Test
    void shouldUpdateCache() {
        SystemUserCacheService cache = new SystemUserCacheService("abc", LocalDateTime.now());
        cache.updateCache("def");

        assertThat(cache.getCachedToken()).isEqualTo("def");
    }

    @Test
    void shouldGetCachedToken() {
        SystemUserCacheService cache = new SystemUserCacheService("abc", LocalDateTime.now());

        assertThat(cache.getCachedToken()).isEqualTo("abc");
    }
}
