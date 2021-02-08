package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

// TODO: 03/02/2021 Add tests
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserService {
    private final IdamClient idam;
    private final RequestData requestData;

    public String getUserEmail() {
        return getUserDetails().getEmail();
    }

    public boolean hasUserRole(UserRole userRole) {
        return requestData.userRoles().contains(userRole.getRoleName());
    }

    public boolean isHmctsUser() {
        return requestData.userRoles().stream().anyMatch(UserRole::isHmctsUser);
    }

    private UserDetails getUserDetails() {
        return idam.getUserDetails(requestData.authorisation());
    }
}
