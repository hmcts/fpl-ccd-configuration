package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import feign.FeignException;
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

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrganisationService {
    private final LocalAuthorityUserLookupConfiguration localAuthorityUserLookupConfiguration;
    private final OrganisationApi organisationApi;
    private final AuthTokenGenerator authTokenGenerator;

    public List<String> findUserIdsInSameOrganisation(String authorisation, String localAuthorityCode) {

        try {
            return ImmutableList
                .copyOf(getUsersFromSameOrganisationBasedOnAppConfig(localAuthorityCode));
        } catch (UnknownLocalAuthorityCodeException ex) {
            try {
                return
                    ImmutableList
                        .copyOf(getUsersFromSameOrganisationBasedOnReferenceData(authorisation));
            } catch (Exception e) {
                throw new UserOrganisationLookupException(
                    format("Can't find users for %s local authority", localAuthorityCode), e
                );
            }
        }
    }

    private List<String> getUsersFromSameOrganisationBasedOnAppConfig(String localAuthorityCode) {
        return localAuthorityUserLookupConfiguration.getUserIds(localAuthorityCode);
    }

    private List<String> getUsersFromSameOrganisationBasedOnReferenceData(String authorisation) {
        return organisationApi
            .findUsersByOrganisation(authorisation, authTokenGenerator.generate(), Status.ACTIVE)
            .getUsers()
            .stream()
            .map(User::getUserIdentifier)
            .collect(toList());
    }

    public Optional<String> findUserByEmail(String authorisation, String email) {
        try {
            return Optional.of(organisationApi.findUserByEmail(authorisation, authTokenGenerator.generate(), email)
                .getUserIdentifier());
        } catch (FeignException.NotFound notFoundException) {
            log.debug("User with email {} not found", email);
            return Optional.empty();
        }
    }
}
