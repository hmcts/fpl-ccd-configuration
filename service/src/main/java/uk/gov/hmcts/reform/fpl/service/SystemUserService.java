package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamClient;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SystemUserService {

    private final SystemUpdateUserConfiguration userConfig;
    private final SystemUserCacheService cacheService;
    private final IdamClient idamClient;

    public String getSysUserToken() {
        if (!userConfig.isCacheTokenEnabled()) {
            return idamClient.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        } else if (!cacheService.isCacheValid()) {
            log.info("Cache invalid/missing, requesting new token");
            cacheService.updateCache(idamClient.getAccessToken(userConfig.getUserName(), userConfig.getPassword()));
        } else {
            log.info("Cache valid, using token");
        }
        return cacheService.getCachedToken();
    }

    public String getUserId(String userToken) {
        return idamClient.getUserInfo(userToken).getUid();
    }

}
