package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.model.UserId;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityUserLookupConfiguration;

import java.util.List;
import java.util.Map;

@Service
public class LocalAuthorityUserService {

    private static final String JURISDICTION = "PUBLICLAW";
    private static final String CASE_TYPE = "Shared_Storage_DRAFTType";

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
        List<String> userIds = lookUpCode(caseLocalAuthority);

        userIds.forEach((id) -> caseAccessApi.grantAccessToCase(
            authorization,
            authTokenGenerator.generate(),
            userId,
            JURISDICTION,
            CASE_TYPE,
            caseId,
            new UserId(id)));
    }

    private List<String> lookUpCode(String localAuthorityCode) {
        Map<String, List<String>> lookupTable = localAuthorityUserLookupConfiguration.getLookupTable();

//        if(lookupTable.get(localAuthorityCode) == null) {
//            throw new
//        }

        return lookupTable.get(localAuthorityCode);
    }
}
