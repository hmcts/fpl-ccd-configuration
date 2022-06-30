package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
import java.util.Optional;
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

    public boolean isChildSolicitor(Long caseId) {
        return hasAnyCaseRoleFrom(CaseRole.childSolicitors(), caseId);
    }

    public boolean isRespondentSolicitor(Long caseId) {
        return hasAnyCaseRoleFrom(CaseRole.respondentSolicitors(), caseId);
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

    // TODO - do these belong here or in another service
    public Optional<WithSolicitor> getRepresentedRespondent(CaseData caseData) {
        Set<CaseRole> roles = getCaseRoles(caseData.getId());
        List<CaseRole> respondentSolicitorRoles = CaseRole.respondentSolicitors();
        // Check if they are a respondent solicitor
        for (int i = 0; i < respondentSolicitorRoles.size(); i++) {
            if (roles.contains(respondentSolicitorRoles.get(i))) {
                if (i > caseData.getRespondents1().size()) {
                    // this respondent doesn't exist so cannot have a solicitor
                    return Optional.empty();
                }
                return Optional.ofNullable(caseData.getRespondents1().get(i).getValue());
            }
        }
        return Optional.empty();
    }

    public Optional<WithSolicitor> getRepresentedChild(CaseData caseData) {
        Set<CaseRole> roles = getCaseRoles(caseData.getId());
        List<CaseRole> childSolicitorRoles = CaseRole.childSolicitors();
        // Check if they are a respondent solicitor
        for (int i = 0; i < childSolicitorRoles.size(); i++) {
            if (roles.contains(childSolicitorRoles.get(i))) {
                if (i > caseData.getChildren1().size()) {
                    // this child doesn't exist so cannot have a solicitor
                    return Optional.empty();
                }
                return Optional.ofNullable(caseData.getChildren1().get(i).getValue());
            }
        }
        return Optional.empty();
    }

    public Optional<WithSolicitor> caseRoleToRepresented(CaseData caseData) {
        if (isRespondentSolicitor(caseData.getId())) {
            return getRepresentedRespondent(caseData);
        } else if (isChildSolicitor(caseData.getId())) {
            return getRepresentedChild(caseData);
        } else {
            return Optional.empty();
        }
    }

}
