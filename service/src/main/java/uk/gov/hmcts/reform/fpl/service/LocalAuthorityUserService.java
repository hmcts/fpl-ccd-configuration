package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.Set;

import static uk.gov.hmcts.reform.fpl.enums.CaseRole.CREATOR;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LocalAuthorityUserService {

    private static final Set<CaseRole> CASE_ROLES = Set.of(LASOLICITOR, CREATOR);
    private final CaseRoleService caseRoleService;
    private final RequestData requestData;
    private final OrganisationService organisationService;

    public void grantUserAccessWithCaseRole(String caseId, String caseLocalAuthority) {
        String currentUser = requestData.userId();
        Organisation organisation = organisationService.findOrganisation();
        if (organisation != null) {
            String organisationId = organisation.getOrganisationIdentifier();
            if (Strings.isNullOrEmpty(organisationId)) {
                caseRoleService.grantAccessToLocalAuthority(caseId, caseLocalAuthority, CASE_ROLES,
                                                            Set.of(currentUser));
                caseRoleService.grantAccessToUser(caseId, currentUser, CASE_ROLES);
            } else {
                organisationService.addCaseUserRoles(organisationId);
            }
        } else {
            caseRoleService.grantAccessToLocalAuthority(caseId, caseLocalAuthority, CASE_ROLES, Set.of(currentUser));
            caseRoleService.grantAccessToUser(caseId, currentUser, CASE_ROLES);
        }
    }
}
