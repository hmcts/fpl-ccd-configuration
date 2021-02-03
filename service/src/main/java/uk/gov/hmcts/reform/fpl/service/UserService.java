package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;

// TODO: 03/02/2021 Add tests
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserService {
    private final IdamClient idam;
    private final RequestData requestData;
    private final CaseAccessDataStoreApi caseAccessApi;
    private final AuthTokenGenerator authTokenGenerator;

    public String getUserEmail() {
        return getUserDetails().getEmail();
    }

    public boolean hasUserRole(UserRole userRole) {
        return requestData.userRoles().contains(userRole.getRoleName());
    }

    public boolean isHmctsUser() {
        return requestData.userRoles().stream().anyMatch(UserRole::isHmctsUser);
    }

    public boolean hasCaseRole(CaseRole role, String caseId) {
        CaseAssignedUserRolesResource userRoles = caseAccessApi.getUserRoles(
            requestData.authorisation(), authTokenGenerator.generate(), List.of(caseId), List.of(requestData.userId())
        );

        return userRoles.getCaseAssignedUserRoles().stream()
            .anyMatch(userCaseRoles -> role.formattedName().equals(userCaseRoles.getCaseRole()));
    }

    private UserDetails getUserDetails() {
        return idam.getUserDetails(requestData.authorisation());
    }
}
