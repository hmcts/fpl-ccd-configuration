package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityUserLookupConfiguration;
import uk.gov.hmcts.reform.fpl.exceptions.UnknownLocalAuthorityCodeException;
import uk.gov.hmcts.reform.fpl.exceptions.UserOrganisationLookupException;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.Status;
import uk.gov.hmcts.reform.rd.model.User;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrganisationService {
    private final LocalAuthorityUserLookupConfiguration localAuthorityUserLookupConfiguration;
    private final OrganisationApi organisationApi;
    private final AuthTokenGenerator authTokenGenerator;

    public List<String> findUserIdsInSameOrganisation(String authorisation, String localAuthorityCode) {
        List<String> userIds = new ArrayList<>();

        try {
            addUsersFromSameOrganisationBasedOnAppConfig(localAuthorityCode, userIds);
        } catch (UnknownLocalAuthorityCodeException ex) {
            try {
                addUsersFromSameOrganisationBasedOnReferenceData(authorisation, userIds);
            } catch (Exception e) {
                throw new UserOrganisationLookupException(
                    format("Can't find users for %s local authority", localAuthorityCode), e
                );
            }
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
