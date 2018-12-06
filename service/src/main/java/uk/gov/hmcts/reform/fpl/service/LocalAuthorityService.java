package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityCodeLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.exceptions.UnknownLocalAuthorityDomainException;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Gets a Local Authority name.
 */
@Service
public class LocalAuthorityService {

    private final IdamApi idamApi;
    private final LocalAuthorityCodeLookupConfiguration localAuthorityCodeLookupConfiguration;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;

    @Autowired
    public LocalAuthorityService(IdamApi idamApi,
                                 LocalAuthorityCodeLookupConfiguration localAuthorityCodeLookupConfiguration,
                                 LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration) {
        this.idamApi = idamApi;
        this.localAuthorityCodeLookupConfiguration = localAuthorityCodeLookupConfiguration;
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
    }

    /**
     * Returns a value for email domain to be stored in Case Data.
     *
     * @param authorization IDAM authorisation token.
     * @return caseLocalAuthority for user.
     */
    public String getLocalAuthorityCode(String authorization) {
        UserDetails userDetails = idamApi.retrieveUserDetails(authorization);
        String email = userDetails.getEmail();
        String domain = extractEmailDomain(email);

        return lookUpCode(domain);
    }

    private String extractEmailDomain(String email) {
        int start = email.indexOf('@');

        return email.toLowerCase().substring(start + 1);
    }

    private String lookUpCode(String emailDomain) {
        Map<String, String> lookupTable = localAuthorityCodeLookupConfiguration.getLookupTable();

        if (lookupTable.get(emailDomain) == null) {
            throw new UnknownLocalAuthorityDomainException(emailDomain + " not found");
        }

        return lookupTable.get(emailDomain);
    }

    public String getLocalAuthorityName(String localAuthorityCode) {
        checkNotNull(localAuthorityCode, "No local authority found");
        return localAuthorityNameLookupConfiguration.getLookupTable().get(localAuthorityCode);
    }
}
