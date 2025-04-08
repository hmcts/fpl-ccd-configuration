package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiFeatureFlag;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Objects;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.config.CacheConfiguration.CAFCASS_API_IDAM_CACHE;
import static uk.gov.hmcts.reform.fpl.config.CacheConfiguration.CAFCASS_API_IDAM_CACHE_MANAGER;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassInterceptorService {
    private final FeatureToggleService featureToggleService;
    private final ObjectProvider<IdamClient> idamClient;

    public boolean isCafcassApiToggledOn() {
        CafcassApiFeatureFlag featureFlag = featureToggleService.getCafcassAPIFlag();
        return isNotEmpty(featureFlag) && featureFlag.isEnableApi();
    }

    @Cacheable(cacheManager = CAFCASS_API_IDAM_CACHE_MANAGER, cacheNames = CAFCASS_API_IDAM_CACHE)
    public UserInfo getIdamUserInfo(String authToken) {
        return Objects.requireNonNull(idamClient.getIfAvailable()).getUserInfo(authToken);
    }
}
