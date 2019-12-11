package uk.gov.hmcts.reform.fpl.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
@Slf4j
public class LocalAuthorityUserService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final CaseUserApi caseUserApi;
    private final OrganisationService organisationService;
    private SystemUpdateUserConfiguration userConfig;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamClient client;
    private final Set<String> caseRoles = Set.of("[LASOLICITOR]", "[CREATOR]");

    @Autowired
    public LocalAuthorityUserService(OrganisationService organisationService,
                                     AuthTokenGenerator authTokenGenerator,
                                     CaseUserApi caseUserApi,
                                     IdamClient idamClient,
                                     SystemUpdateUserConfiguration userConfig) {
        this.organisationService = organisationService;
        this.authTokenGenerator = authTokenGenerator;
        this.caseUserApi = caseUserApi;
        this.client = idamClient;
        this.userConfig = userConfig;
    }

    public void grantUserAccessWithCaseRole(String authorisation,
                                            String userId,
                                            String caseId,
                                            String caseLocalAuthority) {
        List<String> userIds = findUserIds(authorisation, caseLocalAuthority);

        Stream.concat(userIds.stream(), Stream.of(userId))
            .distinct()
            .forEach(id -> {
                try {
                    String authentication = client.authenticateUser(userConfig.getUserName(), userConfig.getPassword());
                    caseUserApi.updateCaseRolesForUser(authentication, authTokenGenerator.generate(), caseId, id,
                        new CaseUser(id, caseRoles));

                    logger.info("Added case roles {} to user {}", caseRoles, id);
                } catch (Exception exception) {
                    logger.warn("Error adding case roles {} to user {}",
                        caseRoles, id, exception);
                }
            });
    }

    private List<String> findUserIds(String authorisation, String localAuthorityCode) {
        try {
            return organisationService
                .findUserIdsInSameOrganisation(authorisation, localAuthorityCode);
        } catch (Exception e) {
            log.warn("Exception while looking for users within the same LA. " +
                "Only the callerId will be given access to the case", e);
            return List.of();
        }
    }
}
