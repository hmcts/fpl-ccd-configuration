package uk.gov.hmcts.reform.fpl.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.exceptions.NoAssociatedUsersException;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.List;
import java.util.Set;

@Service
public class LocalAuthorityUserService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final CaseAccessApi caseAccessApi;
    private final CaseUserApi caseUserApi;
    private final OrganisationService organisationService;
    private SystemUpdateUserConfiguration userConfig;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamClient client;
    private final Set<String> caseRoles = Set.of("[LASOLICITOR]","[CREATOR]");

    @Autowired
    public LocalAuthorityUserService(CaseAccessApi caseAccessApi,
                                     OrganisationService organisationService,
                                     AuthTokenGenerator authTokenGenerator,
                                     CaseUserApi caseUserApi,
                                     IdamClient idamClient,
                                     SystemUpdateUserConfiguration userConfig) {
        this.caseAccessApi = caseAccessApi;
        this.organisationService = organisationService;
        this.authTokenGenerator = authTokenGenerator;
        this.caseUserApi = caseUserApi;
        this.client = idamClient;
        this.userConfig = userConfig;
    }

    public void grantUserAccessWithCaseRole(String authorization, String caseId, String caseLocalAuthority) {
        findUserIds(authorization, caseLocalAuthority).stream()
            .forEach(userId -> {
                try {
                    String authentication = client.authenticateUser(userConfig.getUserName(), userConfig.getPassword());
                    caseUserApi.updateCaseRolesForUser(authentication, authTokenGenerator.generate(), caseId, userId,
                        new CaseUser(userId, caseRoles));

                    logger.info("Added case roles {} to user {}", caseRoles, userId);
                } catch (Exception exception) {
                    logger.warn("Error adding case roles {} to user {}",
                        caseRoles, userId, exception);
                }
            });
    }

    private List<String> findUserIds(String authorization, String localAuthorityCode) {
        List<String> userIds = organisationService.findUserIdsInSameOrganisation(authorization, localAuthorityCode);

        if (userIds.isEmpty()) {
            throw new NoAssociatedUsersException("No users found for the local authority '" + localAuthorityCode + "'");
        }

        return userIds;
    }
}
