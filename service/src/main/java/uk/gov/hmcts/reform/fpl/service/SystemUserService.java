package uk.gov.hmcts.reform.fpl.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

import static com.google.common.base.Suppliers.memoizeWithExpiration;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static uk.gov.hmcts.reform.idam.client.IdamClient.BEARER_AUTH_TYPE;

@Slf4j
@Service
public class SystemUserService {
    private Duration EVICTION_MARGIN = Duration.ofMinutes(10);
    private static final int MAX_TOKEN_CACHE = 8;
    private final SystemUpdateUserConfiguration userConfig;
    private final IdamClient idamClient;


    private LoadingCache<SystemUpdateUserConfiguration, String> accessTokenCache;

    private Supplier<String> userId;

    public SystemUserService(
        SystemUpdateUserConfiguration userConfig,
        IdamClient idamClient,
        @Value("${cache.system_user.access_token:PT1H}") Duration tokenCacheDuration,
        @Value("${cache.system_user.id:PT1H}") Duration idCacheDuration) {

        if (tokenCacheDuration.toHours() >= MAX_TOKEN_CACHE) {
            throw new IllegalArgumentException(String.format(
                "System user token cache duration %s must be shorter than %d hours",
                tokenCacheDuration, MAX_TOKEN_CACHE));
        }

        this.userConfig = userConfig;
        this.idamClient = idamClient;

        log.info("Access token cache duration {} [min]", tokenCacheDuration.toMinutes());
        log.info("Id cache duration {} [min]", idCacheDuration.toMinutes());


        userId = memoizeWithExpiration(() -> fetchId(), idCacheDuration.toMillis(), MILLISECONDS);
        accessTokenCache = CacheBuilder.newBuilder()
            .expireAfterWrite(tokenCacheDuration)
            .build(
                new CacheLoader<>() {
                    @Override
                    public String load(SystemUpdateUserConfiguration key) {
                        return fetchAccessToken();
                    }
                });
    }

    public String getAccessToken() {
        return BEARER_AUTH_TYPE + " " + getAccessTokenInternal();
    }

    private String getAccessTokenInternal() {
        try {
            JWT jwt = JWTParser.parse(accessTokenCache.getUnchecked(userConfig));
            if (Instant.now().isAfter(jwt.getJWTClaimsSet().getExpirationTime().toInstant().minus(EVICTION_MARGIN))) {
                accessTokenCache.invalidate(userConfig);
            }
            return accessTokenCache.getUnchecked(userConfig);
        } catch (ParseException e) {
            accessTokenCache.invalidate(userConfig);
            return accessTokenCache.getUnchecked(userConfig);
        }
    }

    public String getId() {
        return userId.get();
    }

    private String fetchAccessToken() {
        return idamClient.getAccessTokenResponse(userConfig.getUserName(), userConfig.getPassword()).accessToken;
    }

    private String fetchId() {
        return idamClient.getUserInfo(this.getAccessToken()).getUid();
    }
}
