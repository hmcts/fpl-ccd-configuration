package uk.gov.hmcts.reform.fpl.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.exceptions.GrantCaseAccessException;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.rd.model.Organisation;
import uk.gov.hmcts.reform.rd.model.OrganisationUser;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.ALLOCATED_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.HEARING_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.LegalAdviserRole.ALLOCATED_LEGAL_ADVISER;
import static uk.gov.hmcts.reform.fpl.enums.LegalAdviserRole.HEARING_LEGAL_ADVISER;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseAccessService {

    private final CaseAssignmentApi caseAssignmentApi;
    private final RequestData requestData;
    private final AuthTokenGenerator authTokenGenerator;
    private final SystemUserService systemUserService;
    private final OrganisationService organisationService;

    private final List<String> excludedInternalWACaseRoles = List.of(ALLOCATED_JUDGE.getRoleName(),
        HEARING_JUDGE.getRoleName(), ALLOCATED_LEGAL_ADVISER.getRoleName(), HEARING_LEGAL_ADVISER.getRoleName());

    //TO-DO remove once FPLA-2946 migration is done
    public void grantCaseRoleToUsers(Long caseId, Set<String> userIds, CaseRole caseRole) {
        grantCaseAccess(caseId, userIds, caseRole);
        log.info("Users {} granted {} to case {}", userIds, caseRole, caseId);
    }

    public void grantCaseRoleToUser(Long caseId, String userId, CaseRole caseRole) {
        grantCaseAccess(caseId, Set.of(userId), caseRole);
        log.info("User {} granted {} to case {}", userId, caseRole, caseId);
    }

    public void grantCaseRoleToLocalAuthority(Long caseId, String creatorId, String localAuthority, CaseRole caseRole) {
        Set<String> users = getLocalAuthorityUsers(caseId, localAuthority, caseRole);
        users.add(creatorId);

        try {
            grantCaseAccess(caseId, users, caseRole);
            log.info("Users {} granted {} to case {}", users, caseRole, caseId);
        } catch (GrantCaseAccessException ex) {
            // default back to just the default user
            grantCaseRoleToUser(caseId, creatorId, caseRole);
            log.info("ONLY Creator {} granted access {} to case {}", creatorId, caseRole, caseId);
        }
    }

    public void revokeCaseRoleFromUser(Long caseId, String userId, CaseRole caseRole) {
        final String userToken = systemUserService.getSysUserToken();
        final String serviceToken = authTokenGenerator.generate();

        CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest = CaseAssignmentUserRolesRequest.builder()
            .caseAssignmentUserRolesWithOrganisation(List.of(CaseAssignmentUserRoleWithOrganisation.builder()
                .userId(userId)
                .caseRole(caseRole.formattedName())
                .caseDataId(caseId.toString())
                .build()))
            .build();

        caseAssignmentApi.removeCaseUserRoles(userToken, serviceToken, caseAssignmentUserRolesRequest);

        log.info("User {} revoked {} to case {}", userId, caseRole, caseId);
    }

    public Set<CaseRole> getUserCaseRoles(Long caseId) {
        CaseAssignmentUserRolesResource userRolesResource = caseAssignmentApi.getUserRoles(
            requestData.authorisation(), authTokenGenerator.generate(),
            List.of(caseId.toString()), List.of(requestData.userId()));

        return userRolesResource.getCaseAssignmentUserRoles().stream()
            .map(CaseAssignmentUserRole::getCaseRole)
            .filter(role -> !excludedInternalWACaseRoles.contains(role))
            .map(CaseRole::from)
            .collect(Collectors.toSet());
    }

    private void grantCaseAccess(Long caseId, Set<String> users, CaseRole caseRole) {
        try {
            final String userToken = systemUserService.getSysUserToken();
            final String serviceToken = authTokenGenerator.generate();

            final String organisationId = organisationService.findOrganisation()
                .map(Organisation::getOrganisationIdentifier)
                .orElse(null);

            List<CaseAssignmentUserRoleWithOrganisation> caseAssignmentRoles = users.stream()
                .map(user -> CaseAssignmentUserRoleWithOrganisation.builder()
                    .caseDataId(caseId.toString())
                    .organisationId(organisationId)
                    .userId(user)
                    .caseRole(caseRole.formattedName())
                    .build())
                .collect(Collectors.toList());

            CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest =
                CaseAssignmentUserRolesRequest.builder()
                    .caseAssignmentUserRolesWithOrganisation(caseAssignmentRoles)
                    .build();

            caseAssignmentApi.addCaseUserRoles(userToken, serviceToken, caseAssignmentUserRolesRequest);
        } catch (FeignException ex) {
            log.error("Could not assign the users to the case", ex);
            throw new GrantCaseAccessException(caseId, users, caseRole);
        }
    }

    public List<OrganisationUser> getLocalAuthorityUsersAllInfo() {
        return organisationService.getUsersFromSameOrganisationBasedOnReferenceDataAllInfo();
    }

    private Set<String> getLocalAuthorityUsers(Long caseId, String localAuthority, CaseRole caseRole) {
        try {
            return organisationService.findUserIdsInSameOrganisation(localAuthority);
        } catch (Exception e) {
            throw new GrantCaseAccessException(caseId, localAuthority, caseRole, e);
        }
    }
}
