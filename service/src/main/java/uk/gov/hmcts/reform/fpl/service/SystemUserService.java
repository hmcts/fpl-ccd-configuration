package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SystemUserService {

    private final SystemUpdateUserConfiguration userConfig;
    private final IdamClient idamClient;
    private String cachedToken;
    private LocalDateTime timeLastCached;

    public String getSysUserToken() {
        // no cached token? no cached time? token > 2 hours old? -> Generate new token
        if (isEmpty(cachedToken) || isEmpty(timeLastCached)
            || timeLastCached.until(LocalDateTime.now(), ChronoUnit.MINUTES) > 5) {
            cachedToken = idamClient.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
            timeLastCached = LocalDateTime.now();
            log.info("Requested new IDAM system-user token");
        } else {
            log.info("Using cached IDAM system-user token");
        }
        return cachedToken;
    }

    public String getUserId(String userToken) {
        return idamClient.getUserInfo(userToken).getUid();
    }

}
