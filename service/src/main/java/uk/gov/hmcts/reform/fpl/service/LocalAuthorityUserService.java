package uk.gov.hmcts.reform.fpl.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.model.UserId;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityUserLookupConfiguration;
import uk.gov.hmcts.reform.fpl.exceptions.NoAssociatedUsersException;
import uk.gov.hmcts.reform.fpl.exceptions.UnknownLocalAuthorityCodeException;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

    public void grantUserAccess(String authorization, String userId, String caseId, String caseLocalAuthority) {
        List<String> userIds = new LinkedList<>(findUserIds(caseLocalAuthority));
        userIds.remove(userId);

        userIds.forEach((id) -> {
            logger.debug("Granting user {} access to case {}", id, caseId);

            caseAccessApi.grantAccessToCase(
                authorization, authTokenGenerator.generate(), userId, JURISDICTION, CASE_TYPE, caseId, new UserId(id));
        });
    }

    private List<String> findUserIds(String localAuthorityCode) {
        Map<String, List<String>> lookupTable = localAuthorityUserLookupConfiguration.getLookupTable();

        if (lookupTable.get(localAuthorityCode) == null) {
            throw new UnknownLocalAuthorityCodeException(
                "The local authority: " + localAuthorityCode + " was not found");
        }

        if (lookupTable.get(localAuthorityCode).isEmpty()) {
            throw new NoAssociatedUsersException("No users found for the local authority: " + localAuthorityCode);
        }

        return lookupTable.get(localAuthorityCode);
    }
}
