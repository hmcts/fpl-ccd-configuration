package uk.gov.hmcts.reform.fpl.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.model.AddCaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.exceptions.GrantCaseAccessException;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseAccessService {

    private final IdamClient idam;
    private final CaseAccessDataStoreApi caseAccessDataStoreApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final SystemUpdateUserConfiguration userConfig;
    private final OrganisationService organisationService;

    //TO-DO remove once FPLA-2946 migration is done
    public void grantCaseRoleToUsers(Long caseId, Set<String> userIds, CaseRole caseRole) {
        grantCaseAccess(caseId, userIds, caseRole);
        log.info("Users {} granted {} to case {}", userIds, caseRole, caseId);
    }

    public void grantCaseRoleToUser(Long caseId, String userId, CaseRole caseRole) {
        grantCaseAccess(caseId, Set.of(userId), caseRole);
        log.info("User {} granted {} to case {}", userId, caseRole, caseId);
    }

    public void grantCaseRoleToLocalAuthority(Long caseId, String localAuthority, CaseRole caseRole) {
        Set<String> localAuthorityUsers = getUsers(caseId, localAuthority, caseRole);
        grantCaseAccess(caseId, localAuthorityUsers, caseRole);
        log.info("Users {} granted {} to case {}", localAuthorityUsers, caseRole, caseId);
    }

    public void revokeCaseRoleFromUser(Long caseId, String userId, CaseRole caseRole) {
        final String userToken = idam.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        final String serviceToken = authTokenGenerator.generate();

        CaseAssignedUserRolesRequest caseAssignedUserRolesRequest = CaseAssignedUserRolesRequest.builder()
            .caseAssignedUserRoles(List.of(CaseAssignedUserRoleWithOrganisation.builder()
                .userId(userId)
                .caseRole(caseRole.formattedName())
                .caseDataId(caseId.toString())
                .build()))
            .build();

        caseAccessDataStoreApi.removeCaseUserRoles(userToken, serviceToken, caseAssignedUserRolesRequest);
    }

    private void grantCaseAccess(Long caseId, Set<String> users, CaseRole caseRole) {
        try {
            final String userToken = idam.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
            final String serviceToken = authTokenGenerator.generate();

            final String organisationId = organisationService.findOrganisation()
                .map(Organisation::getOrganisationIdentifier)
                .orElse(null);

            List<CaseAssignedUserRoleWithOrganisation> caseAssignedRoles = users.stream()
                .map(user -> CaseAssignedUserRoleWithOrganisation.builder()
                    .caseDataId(caseId.toString())
                    .organisationId(organisationId)
                    .userId(user)
                    .caseRole(caseRole.formattedName())
                    .build())
                .collect(Collectors.toList());

            AddCaseAssignedUserRolesRequest addCaseAssignedUserRolesRequest =
                AddCaseAssignedUserRolesRequest.builder()
                    .caseAssignedUserRoles(caseAssignedRoles)
                    .build();

            caseAccessDataStoreApi.addCaseUserRoles(userToken, serviceToken, addCaseAssignedUserRolesRequest);
        } catch (FeignException ex) {
            log.error("Could not assign the users to the case", ex);
            throw new GrantCaseAccessException(caseId, users, caseRole);
        }
    }

    private Set<String> getUsers(Long caseId, String localAuthority, CaseRole caseRole) {
        try {
            return organisationService.findUserIdsInSameOrganisation(localAuthority);
        } catch (Exception e) {
            throw new GrantCaseAccessException(caseId, localAuthority, caseRole, e);
        }
    }
}
