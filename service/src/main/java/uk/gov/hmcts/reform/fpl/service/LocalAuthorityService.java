package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityCodeLookupConfiguration;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

/**
 * Gets a Local Authority name.
 */
@Service
public class LocalAuthorityService {

    private final IdamClient idamClient;
    private final LocalAuthorityCodeLookupConfiguration localAuthorityCodeLookupConfiguration;
    private final RequestData requestData;

    @Autowired
    public LocalAuthorityService(IdamClient idamClient,
                                 LocalAuthorityCodeLookupConfiguration localAuthorityCodeLookupConfiguration,
                                 RequestData requestData) {
        this.idamClient = idamClient;
        this.localAuthorityCodeLookupConfiguration = localAuthorityCodeLookupConfiguration;
        this.requestData = requestData;
    }

    /**
     * Returns a value for email domain to be stored in Case Data.
     *
     * @return caseLocalAuthority for user.
     */
    public String getLocalAuthorityCode() {
        UserInfo userInfo = idamClient.getUserInfo(requestData.authorisation());
        String email = userInfo.getSub();
        String domain = extractEmailDomain(email);

        return localAuthorityCodeLookupConfiguration.getLocalAuthorityCode(domain);
    }

    private String extractEmailDomain(String email) {
        int start = email.indexOf('@');

        return email.toLowerCase().substring(start + 1);
    }
}
