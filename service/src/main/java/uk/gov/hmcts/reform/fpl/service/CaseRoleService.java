package uk.gov.hmcts.reform.fpl.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.model.AddCaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.ccd.model.AddCaseAssignedUserRolesResponse;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRoleWithOrganisation;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.exceptions.GrantCaseAccessException;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseRoleService {

    private final IdamClient idam;
    private final CaseAccessDataStoreApi caseAccessDataStoreApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final SystemUpdateUserConfiguration userConfig;
    private final OrganisationService organisationService;

    public void grantCaseAssignmentToLocalAuthority(String caseId, String localAuthority, Set<CaseRole> roles) {
        Set<String> localAuthorityUsers = getUsers(caseId, localAuthority, Collections.emptySet(), roles);
        grantCaseAssignmentAccess(caseId, localAuthorityUsers, roles);
        log.info("Users {} granted {} to case {}", localAuthorityUsers, roles, caseId);
    }

    private void grantCaseAssignmentAccess(String caseId, Set<String> users, Set<CaseRole> roles) {
        try {
            final String userToken = idam.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
            final String serviceToken = authTokenGenerator.generate();

            final String organisationId = organisationService.findOrganisation()
                .map(Organisation::getOrganisationIdentifier)
                .orElse(null);

            List<CaseAssignedUserRoleWithOrganisation> caseAssignedRoles = users.stream()
                .map(user -> CaseAssignedUserRoleWithOrganisation.builder()
                    .caseDataId(caseId)
                    .organisationId(organisationId)
                    .userId(user)
                    .caseRole(CaseRole.LASOLICITOR.formattedName())
                    .build())
                .collect(Collectors.toList());

            AddCaseAssignedUserRolesRequest addCaseAssignedUserRolesRequest =
                AddCaseAssignedUserRolesRequest.builder()
                    .caseAssignedUserRoles(caseAssignedRoles)
                    .build();

            AddCaseAssignedUserRolesResponse response =
                caseAccessDataStoreApi.addCaseUserRoles(userToken, serviceToken, addCaseAssignedUserRolesRequest);
            log.info("Case Assignment Status {}", response.getStatus());

        } catch (FeignException ex) {
            log.error("Could not assign the users to the case", ex);
            throw new GrantCaseAccessException(caseId, users, roles);
        }
    }

    private Set<String> getUsers(String caseId, String localAuthority, Set<String> excludedUsers, Set<CaseRole> roles) {
        try {
            return organisationService.findUserIdsInSameOrganisation(localAuthority).stream()
                .filter(userId -> !excludedUsers.contains(userId))
                .collect(toSet());
        } catch (Exception e) {
            throw new GrantCaseAccessException(caseId, localAuthority, roles, e);
        }
    }
}
