package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.UserEmailLookupConfiguration;

import java.util.List;

@Service
public class EmailLookUpService {

    private final UserEmailLookupConfiguration userEmailLookupConfiguration;

    @Autowired
    public EmailLookUpService(UserEmailLookupConfiguration userEmailLookupConfiguration) {
        this.userEmailLookupConfiguration = userEmailLookupConfiguration;
    }

    public List<String> getEmails(String localAuthorityCode) {
        return findEmails(localAuthorityCode);
    }

    private List<String> findEmails(String localAuthorityCode) {
        return userEmailLookupConfiguration.getLookupTable().get(localAuthorityCode);
    }
}
