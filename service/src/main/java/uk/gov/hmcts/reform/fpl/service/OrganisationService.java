package uk.gov.hmcts.reform.fpl.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityUserLookupConfiguration;
import uk.gov.hmcts.reform.fpl.exceptions.UnknownLocalAuthorityCodeException;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.Status;
import uk.gov.hmcts.reform.rd.model.User;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OrganisationService {
    private final LocalAuthorityUserLookupConfiguration localAuthorityUserLookupConfiguration;
    private final OrganisationApi organisationApi;
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public OrganisationService(LocalAuthorityUserLookupConfiguration localAuthorityUserLookupConfiguration,
                               OrganisationApi organisationApi,
                               AuthTokenGenerator authTokenGenerator) {
        this.localAuthorityUserLookupConfiguration = localAuthorityUserLookupConfiguration;
        this.organisationApi = organisationApi;
        this.authTokenGenerator = authTokenGenerator;
    }


    public List<String> findUserIdsInSameOrganisation(String authorisation, String userId, String localAuthorityCode) {
        List<String> userIds = new ArrayList<>();

        try {
            addUsersFromSameOrganisationBasedOnAppConfig(localAuthorityCode, userIds);
        } catch (UnknownLocalAuthorityCodeException ex) {
            try {
                addUsersFromSameOrganisationBasedOnReferenceData(authorisation, userIds);
            } catch (Exception e) {
                log.warn("Can't find LocalAuthority for code: "
                    + localAuthorityCode
                    + " in app config and the PRD endpoint call threw an exception.", e);
            }
        }

        if (!userIds.contains(userId)) {
            userIds.add(userId);
        }

        return userIds;
    }

    private void addUsersFromSameOrganisationBasedOnAppConfig(String localAuthorityCode, List<String> userIds) {
        userIds.addAll(localAuthorityUserLookupConfiguration.getUserIds(localAuthorityCode));
    }

    private void addUsersFromSameOrganisationBasedOnReferenceData(String authorisation, List<String> userIds) {
        organisationApi
            .findUsersByOrganisation(authorisation, authTokenGenerator.generate(), Status.ACTIVE)
            .stream()
            .map(User::getUserIdentifier)
            .forEach(userIds::add);
    }
}
