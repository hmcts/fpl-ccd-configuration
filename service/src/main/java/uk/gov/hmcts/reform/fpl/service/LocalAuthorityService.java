package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityCodeLookupConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

/**
 * Gets a Local Authority name.
 */
@Service
public class LocalAuthorityService {

    private final IdamApi idamApi;
    private final LocalAuthorityCodeLookupConfiguration localAuthorityCodeLookupConfiguration;

    @Autowired
    public LocalAuthorityService(IdamApi idamApi,
                                 LocalAuthorityCodeLookupConfiguration localAuthorityCodeLookupConfiguration) {
        this.idamApi = idamApi;
        this.localAuthorityCodeLookupConfiguration = localAuthorityCodeLookupConfiguration;
    }

    /**
     * Returns a value for email domain to be stored in Case Data.
     *
     * @param authorization IDAM authorisation token.
     * @return caseLocalAuthority for user.
     */
    public String getLocalAuthorityCode(String authorization) {
        UserInfo userInfo = idamApi.retrieveUserInfo(authorization);
        String email = userInfo.getSub();
        String domain = extractEmailDomain(email);

        return localAuthorityCodeLookupConfiguration.getLocalAuthorityCode(domain);
    }

    private String extractEmailDomain(String email) {
        int start = email.indexOf('@');

        return email.toLowerCase().substring(start + 1);
    }
}
