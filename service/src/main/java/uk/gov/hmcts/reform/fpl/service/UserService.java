package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.OrganisationalRole;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.HMCTS_ADMIN;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.JUDICIARY;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserService {
    private final IdamClient idam;
    private final RequestData requestData;
    private final CaseAccessService caseAccessService;
    private final RoleAssignmentService roleAssignmentService;

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

    /**
     * @deprecated use {@link #isCtscUser()} instead - the IDAM roles for court-admins are used for multiple user types
     * and are being phased out in favour of AM Organisational roles.
     * @return true if the user has a `caseworker-publiclaw-courtadmin` IDAM role
     */
    @Deprecated(since = "DFPL-2731", forRemoval = false)
    public boolean isHmctsAdminUser() {
        Set<String> roles = getIdamRoles();
        return roles != null && roles.contains(HMCTS_ADMIN.getRoleName());
    }

    public boolean isJudiciaryUser() {
        Set<String> roles = getIdamRoles();
        return roles != null && roles.contains(JUDICIARY.getRoleName());
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

    public Set<String> getIdamRoles() {
        return requestData.userRoles();
    }

    public Set<OrganisationalRole> getOrgRoles() {
        return roleAssignmentService.getOrganisationalRolesForUser(requestData.userId());
    }

    public Set<String> getJudicialCaseRoles(Long caseId) {
        return roleAssignmentService
            .getJudicialCaseRolesForUserAtTime(requestData.userId(), caseId, ZonedDateTime.now());
    }

    public boolean hasAnyOrgRoleFrom(List<OrganisationalRole> organisationalRoles) {
        Set<OrganisationalRole> roles = getOrgRoles();
        return isNotEmpty(roles) && roles.stream().anyMatch(organisationalRoles::contains);
    }

    public boolean isCtscUser() {
        return this.hasAnyOrgRoleFrom(List.of(OrganisationalRole.CTSC));
    }

    public boolean isCtscOrLocalCourtUser() {
        return this.hasAnyOrgRoleFrom(List.of(OrganisationalRole.CTSC, OrganisationalRole.LOCAL_COURT_ADMIN));
    }


}
