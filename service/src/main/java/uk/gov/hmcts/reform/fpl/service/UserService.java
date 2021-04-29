package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Set;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserService {
    private final IdamClient idam;
    private final RequestData requestData;

    public String getUserEmail() {
        return getUserDetails().getEmail();
    }

    public boolean hasUserRole(UserRole userRole) {
        return getIdamRoles().contains(userRole.getRoleName());
    }

    public boolean isHmctsUser() {
        return getIdamRoles().stream().anyMatch(UserRole::isHmctsUser);
    }

    public UserDetails getUserDetailsById(String userId) {
        return idam.getUserByUserId(requestData.authorisation(), userId);
    }

    private Set<String> getIdamRoles() {
        return requestData.userRoles();
    }

    private UserDetails getUserDetails() {
        return idam.getUserDetails(requestData.authorisation());
    }
}
