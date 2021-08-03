package uk.gov.hmcts.reform.fpl.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityUserLookupConfiguration;
import uk.gov.hmcts.reform.fpl.exceptions.UnknownLocalAuthorityCodeException;
import uk.gov.hmcts.reform.fpl.exceptions.UserLookupException;
import uk.gov.hmcts.reform.fpl.exceptions.UserOrganisationLookupException;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.utils.MaskHelper;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.Organisation;
import uk.gov.hmcts.reform.rd.model.OrganisationUser;
import uk.gov.hmcts.reform.rd.model.Status;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static uk.gov.hmcts.reform.fpl.config.CacheConfiguration.ORGANISATION_CACHE;
import static uk.gov.hmcts.reform.fpl.config.CacheConfiguration.REQUEST_SCOPED_CACHE_MANAGER;
import static uk.gov.hmcts.reform.fpl.utils.MaskHelper.maskEmail;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrganisationService {
    private final LocalAuthorityUserLookupConfiguration localAuthorityUserLookupConfiguration;
    private final OrganisationApi organisationApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final SystemUserService systemUserService;
    private final RequestData requestData;

    public Set<String> findUserIdsInSameOrganisation(String localAuthorityCode) {
        try {
            return new LinkedHashSet<>(getUsersFromSameOrganisationBasedOnReferenceData(requestData.authorisation()));
        } catch (FeignException.NotFound | FeignException.Forbidden unregisteredException) {
            log.warn("User {} from {} not registered in MO. {}", requestData.userId(), localAuthorityCode,
                ExceptionUtils.getStackTrace(unregisteredException));
        } catch (FeignException prdFailureException) {
            log.error("Request for users in same organisation failed", prdFailureException);
        }
        return useLocalMapping(localAuthorityCode);
    }

    public Optional<String> findUserByEmail(String email) {
        try {
            return Optional.of(organisationApi.findUserByEmail(requestData.authorisation(),
                authTokenGenerator.generate(), email).getUserIdentifier());
        } catch (FeignException.NotFound notFoundException) {
            log.info("User with email {} not found", MaskHelper.maskEmail(email));
            return Optional.empty();
        } catch (FeignException exception) {
            throw new UserLookupException(maskEmail(getStackTrace(exception), email));
        }
    }

    @Cacheable(cacheManager = REQUEST_SCOPED_CACHE_MANAGER, cacheNames = ORGANISATION_CACHE)
    public Optional<Organisation> findOrganisation() {
        try {
            return ofNullable(organisationApi.findUserOrganisation(requestData.authorisation(),
                authTokenGenerator.generate()));
        } catch (FeignException.NotFound | FeignException.Forbidden ex) {
            log.error("User not registered in MO", ex);
            return Optional.empty();
        }
    }

    public Optional<Organisation> findOrganisation(String organisationId) {
        try {
            String userToken = systemUserService.getSysUserToken();
            return ofNullable(organisationApi.findOrganisation(userToken,
                authTokenGenerator.generate(), organisationId));
        } catch (FeignException.NotFound | FeignException.Forbidden ex) {
            log.error("Organisation {} not registered", organisationId);
            return Optional.empty();
        }
    }

    private Set<String> useLocalMapping(String localAuthorityCode) {
        try {
            return Set.copyOf(getUsersFromSameOrganisationBasedOnAppConfig(localAuthorityCode));
        } catch (UnknownLocalAuthorityCodeException exception) {
            throw new UserOrganisationLookupException(
                format("Can't find users for %s local authority", localAuthorityCode), exception
            );
        }
    }

    private List<String> getUsersFromSameOrganisationBasedOnAppConfig(String localAuthorityCode) {
        return localAuthorityUserLookupConfiguration.getUserIds(localAuthorityCode);
    }

    private List<String> getUsersFromSameOrganisationBasedOnReferenceData(String authorisation) {
        return organisationApi
            .findUsersInLoggedUserOrganisation(authorisation, authTokenGenerator.generate(), Status.ACTIVE, false)
            .getUsers()
            .stream()
            .map(OrganisationUser::getUserIdentifier)
            .collect(toList());
    }
}
