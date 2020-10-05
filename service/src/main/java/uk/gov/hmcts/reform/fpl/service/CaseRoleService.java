package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.Sets;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.ccd.model.AddCaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.ccd.model.AddCaseAssignedUserRolesResponse;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRoleWithOrganisation;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.exceptions.GrantCaseAccessException;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseRoleService {

    private final IdamClient idam;
    private final CaseUserApi caseUser;
    private final CaseAccessDataStoreApi caseAccessDataStoreApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final SystemUpdateUserConfiguration userConfig;
    private final OrganisationService organisationService;

    public void grantAccessToUser(String caseId, String user, Set<CaseRole> roles) {
        grantCaseAccess(caseId, Set.of(user), roles);
        log.info("User {} granted {} to case {}", user, roles, caseId);
    }

    @Async
    @Retryable(value = {GrantCaseAccessException.class},
        backoff = @Backoff(delayExpression = "#{${retry.delay:1000}}"),
        label = "share a case")
    //Due to @Retryable keep this method idempotent
    public void grantAccessToLocalAuthority(String caseId, String localAuthority, Set<CaseRole> roles,
                                            Set<String> excludeUsers) {
        Set<String> localAuthorityUsers = getUsers(caseId, localAuthority, excludeUsers, roles);
        grantCaseAccess(caseId, localAuthorityUsers, roles);
        log.info("Users {} granted {} to case {}", localAuthorityUsers, roles, caseId);
    }

    private void grantCaseAccess(String caseId, Set<String> users, Set<CaseRole> roles) {
        final Set<String> usersGrantedAccess = Sets.newConcurrentHashSet();

        try {
            final String userToken = idam.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
            final String serviceToken = authTokenGenerator.generate();
            final Set<String> caseRoles = roles.stream()
                .map(CaseRole::formattedName)
                .collect(toSet());

            users.stream().parallel()
                .forEach(userId -> {
                    try {
                        caseUser.updateCaseRolesForUser(
                            userToken, serviceToken, caseId, userId, new CaseUser(userId, caseRoles));
                        usersGrantedAccess.add(userId);
                    } catch (Exception exception) {
                        log.warn("User {} has not been granted {} to case {}", userId, roles, caseId, exception);
                    }
                });
        } finally {
            checkAllUsersGrantedAccess(caseId, users, usersGrantedAccess, roles);
        }
    }

    public void grantCaseAssignmentToUser(String caseId, String user, Set<CaseRole> roles) {
        grantCaseAssignmentAccess(caseId, Set.of(user), roles);
        log.info("User {} granted {} to case {}", user, roles, caseId);
    }

    public void grantCaseAssignmentToLocalAuthority(String caseId, String localAuthority, Set<CaseRole> roles,
                                            Set<String> excludeUsers) {
        Set<String> localAuthorityUsers = getUsers(caseId, localAuthority, excludeUsers, roles);
        grantCaseAccess(caseId, localAuthorityUsers, roles);
        log.info("Users {} granted {} to case {}", localAuthorityUsers, roles, caseId);
    }

    private void grantCaseAssignmentAccess(String caseId, Set<String> users, Set<CaseRole> roles) {
        final Set<String> usersGrantedAccess = Sets.newConcurrentHashSet();

        try {
            final String userToken = idam.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
            final String serviceToken = authTokenGenerator.generate();
            List<CaseAssignedUserRoleWithOrganisation> caseAssignedRoles = new ArrayList<>();
            AddCaseAssignedUserRolesRequest addCaseRequest = new AddCaseAssignedUserRolesRequest();

            users.stream().sequential()
                .forEach(userId -> {
                    try {
                        CaseAssignedUserRoleWithOrganisation caseUserRole = new CaseAssignedUserRoleWithOrganisation();
                        // It has been decided that this will be tested in preview with blank org id first.
                        caseUserRole.setOrganisationId("");
                        caseUserRole.setCaseDataId(caseId);
                        caseUserRole.setUserId(userId);
                        // This api call needs only LASOLICITOR rolestring
                        caseUserRole.setCaseRole(CaseRole.LASOLICITOR.formattedName());
                        caseAssignedRoles.add(caseUserRole);
                        addCaseRequest.setCaseAssignedUserRoles(caseAssignedRoles);
                        usersGrantedAccess.add(userId);
                    } catch (Exception exception) {
                        log.warn("User {} has not been granted {} to case {}", userId, roles, caseId, exception);
                    }
                });

            caseAccessDataStoreApi.addCaseUserRoles(userToken,serviceToken,addCaseRequest);
        } catch (FeignException ex) {
            log.error("Could not find the case users for associated organisation from reference data", ex);
            String statusMessage = ex.getMessage();
            AddCaseAssignedUserRolesResponse addCaseResponse = new AddCaseAssignedUserRolesResponse();
            addCaseResponse.setStatus(statusMessage);
        } finally {
            checkAllUsersGrantedAccess(caseId, users, usersGrantedAccess, roles);
        }
    }

    private void checkAllUsersGrantedAccess(String caseId, Set<String> users, Set<String> usersGrantedAccess,
                                            Set<CaseRole> caseRoles) {
        final Set<String> usersNotGrantedAccess = Sets.difference(users, usersGrantedAccess);

        if (!usersNotGrantedAccess.isEmpty()) {
            throw new GrantCaseAccessException(caseId, usersNotGrantedAccess, caseRoles);
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
