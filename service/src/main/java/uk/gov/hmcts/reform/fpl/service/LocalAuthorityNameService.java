package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.exceptions.UnknownLocalAuthorityDomainException;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Map;

/**
 * Gets a Local Authority name.
 */
@Service
public class LocalAuthorityNameService {

    private final IdamApi idamApi;
    private final LocalAuthorityNameLookupConfiguration localAuthorityLookupConfiguration;

    @Autowired
    public LocalAuthorityNameService(IdamApi idamApi,
                                     LocalAuthorityNameLookupConfiguration localAuthorityLookupConfiguration) {
        this.idamApi = idamApi;
        this.localAuthorityLookupConfiguration = localAuthorityLookupConfiguration;
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
        Map<String, String> lookupTable = localAuthorityLookupConfiguration.getLookupTable();

        if (lookupTable.get(emailDomain) == null) {
            throw new UnknownLocalAuthorityDomainException(emailDomain + " not found");
        }

        return lookupTable.get(emailDomain);
    }
}
