package uk.gov.hmcts.reform.fpl.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.time.Duration;
import java.util.function.Supplier;

import static com.google.common.base.Suppliers.memoizeWithExpiration;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Slf4j
@Service
public class SystemUserService {
    private static final int MAX_TOKEN_CACHE = 8;
    private final SystemUpdateUserConfiguration userConfig;
    private final IdamClient idamClient;

    private Supplier<String> userId;
    private Supplier<String> userToken;

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

        userToken = memoizeWithExpiration(() -> fetchAccessToken(), tokenCacheDuration.toMillis(), MILLISECONDS);
        userId = memoizeWithExpiration(() -> fetchId(), idCacheDuration.toMillis(), MILLISECONDS);
    }

    public String getAccessToken() {
        return userToken.get();
    }

    public String getId() {
        return userId.get();
    }

    private String fetchAccessToken() {
        return idamClient.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
    }

    private String fetchId() {
        return idamClient.getUserInfo(this.getAccessToken()).getUid();
    }
}
