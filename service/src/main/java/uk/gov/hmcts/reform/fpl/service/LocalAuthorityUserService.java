package uk.gov.hmcts.reform.fpl.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.model.UserId;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityUserLookupConfiguration;
import uk.gov.hmcts.reform.fpl.exceptions.NoAssociatedUsersException;
import uk.gov.hmcts.reform.fpl.exceptions.UnknownLocalAuthorityCodeException;

import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

@EnableRetry
@Service
public class LocalAuthorityUserService {

    private static final String JURISDICTION = "PUBLICLAW";
    private static final String CASE_TYPE = "Shared_Storage_DRAFTType";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final CaseAccessApi caseAccessApi;
    private final LocalAuthorityUserLookupConfiguration localAuthorityUserLookupConfiguration;
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public LocalAuthorityUserService(CaseAccessApi caseAccessApi,
                                     LocalAuthorityUserLookupConfiguration localAuthorityUserLookupConfiguration,
                                     AuthTokenGenerator authTokenGenerator) {
        this.caseAccessApi = caseAccessApi;
        this.localAuthorityUserLookupConfiguration = localAuthorityUserLookupConfiguration;
        this.authTokenGenerator = authTokenGenerator;
    }

    @Retryable()
    public void grantUserAccess(String authorization, String creatorUserId, String caseId, String caseLocalAuthority) {
        findUserIds(caseLocalAuthority).stream()
            .filter(userId -> !Objects.equals(userId, creatorUserId))
            .forEach(userId -> {
                logger.debug("Granting user {} access to case {}", userId, caseId);

                try {
                    caseAccessApi.grantAccessToCase(
                        authorization,
                        authTokenGenerator.generate(),
                        creatorUserId,
                        JURISDICTION,
                        CASE_TYPE,
                        caseId,
                        new UserId(userId));

                } catch (Exception ex) {
                    logger.warn("Could not grant user {} access to case {}", userId, caseId, ex);
                }
            });
    }

    private List<String> findUserIds(String localAuthorityCode) {
        checkNotNull(localAuthorityCode, "Case does not have local authority assigned");

        List<String> userIds = localAuthorityUserLookupConfiguration.getLookupTable().get(localAuthorityCode);

        if (userIds == null) {
            throw new UnknownLocalAuthorityCodeException("Local authority '" + localAuthorityCode + "' was not found");
        }

        if (userIds.isEmpty()) {
            throw new NoAssociatedUsersException("No users found for the local authority '" + localAuthorityCode + "'");
        }

        return userIds;
    }
}
