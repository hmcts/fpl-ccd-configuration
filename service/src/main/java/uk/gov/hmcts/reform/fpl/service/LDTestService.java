package uk.gov.hmcts.reform.fpl.service;

import com.launchdarkly.client.LDClient;
import com.launchdarkly.client.LDUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LDTestService {

    private final LDClient ldClient;
    private final IdamApi idamApi;

    public boolean isUserAllowedToCreateCase(String authorization) {
        UserInfo userInfo = idamApi.retrieveUserInfo(authorization);

        LDUser user = new LDUser.Builder(userInfo.getUid())
            .firstName(userInfo.getName())
            .customString("roles", userInfo.getRoles())
            .build();

        return ldClient.boolVariation("case-creation", user, true);
    }
}
