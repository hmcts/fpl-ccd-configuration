package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import static uk.gov.hmcts.reform.fpl.config.CacheConfiguration.REQUEST_SCOPED_CACHE_MANAGER;
import static uk.gov.hmcts.reform.fpl.config.CacheConfiguration.SYS_USER_CACHE;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SystemUserService {

    private final SystemUpdateUserConfiguration userConfig;
    private final IdamClient idamClient;

    @Cacheable(cacheManager = REQUEST_SCOPED_CACHE_MANAGER, cacheNames = SYS_USER_CACHE)
    public String getSysUserToken() {
        return idamClient.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
    }

    @Cacheable(cacheManager = REQUEST_SCOPED_CACHE_MANAGER, cacheNames = SYS_USER_CACHE)
    public String getUserId(String userToken) {
        return idamClient.getUserInfo(userToken).getUid();
    }

}
