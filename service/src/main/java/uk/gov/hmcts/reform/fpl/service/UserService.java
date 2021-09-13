package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.fpl.enums.UserRole.HMCTS_ADMIN;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserService {
    private final IdamClient idam;
    private final RequestData requestData;
    private final CaseAccessService caseAccessService;

    public String getUserEmail() {
        return getUserDetails().getEmail();
    }

    public String getUserName() {
        return getUserDetails().getFullName();
    }

    public boolean hasUserRole(UserRole userRole) {
        return getIdamRoles().contains(userRole.getRoleName());
    }

    public boolean isHmctsUser() {
        return getIdamRoles().stream().anyMatch(UserRole::isHmctsUser);
    }

    public boolean isHmctsAdminUser() {
        Set<String> roles = getIdamRoles();
        return roles != null && roles.contains(HMCTS_ADMIN.getRoleName());
    }

    public boolean hasAnyCaseRoleFrom(List<CaseRole> caseRoles, Long caseId) {
        final Set<CaseRole> userCaseRoles = getCaseRoles(caseId);
        return userCaseRoles.stream().anyMatch(caseRoles::contains);
    }

    public Set<CaseRole> getCaseRoles(Long caseId) {
        return caseAccessService.getUserCaseRoles(caseId);
    }

    public UserDetails getUserDetailsById(String userId) {
        return idam.getUserByUserId(requestData.authorisation(), userId);
    }

    public UserDetails getUserDetails() {
        return idam.getUserDetails(requestData.authorisation());
    }

    private Set<String> getIdamRoles() {
        return requestData.userRoles();
    }

}
