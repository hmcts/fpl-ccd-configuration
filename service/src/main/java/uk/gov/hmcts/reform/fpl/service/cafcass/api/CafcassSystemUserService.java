package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.CafcassSystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import static uk.gov.hmcts.reform.fpl.config.CacheConfiguration.LOCAL_CACHE_MANAGER;
import static uk.gov.hmcts.reform.fpl.config.CacheConfiguration.SYS_USER_CACHE;

@Slf4j
@Service
@EnableCaching
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CafcassSystemUserService {

    private final CafcassSystemUpdateUserConfiguration userConfig;
    private final IdamClient idamClient;

    @Cacheable(cacheManager = LOCAL_CACHE_MANAGER, cacheNames = SYS_USER_CACHE, unless = "#result == null",
        key = "#root.target.SYS_USER_TOKEN_CACHE_KEY")
    public String getSysUserToken() {
        log.info("Requesting cafcass system-user token from IDAM");
        return idamClient.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
    }

    public String getUserSub(String userToken) {
        return idamClient.getUserInfo(userToken).getSub();
    }

}
