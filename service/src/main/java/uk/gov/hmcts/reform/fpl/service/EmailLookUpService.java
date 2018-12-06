package uk.gov.hmcts.reform.fpl.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.UserEmailLookupConfiguration;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@Service
public class EmailLookUpService {

    private final UserEmailLookupConfiguration userEmailLookupConfiguration;
    private final Logger logger = LoggerFactory.getLogger(getClass());


    @Autowired
    public EmailLookUpService(UserEmailLookupConfiguration userEmailLookupConfiguration) {
        this.userEmailLookupConfiguration = userEmailLookupConfiguration;
    }

    public List<String> getEmails(String localAuthorityCode) {
        checkNotNull(localAuthorityCode, "Case does not have local authority assigned");

        List<String> userEmails = userEmailLookupConfiguration.getLookupTable().get(localAuthorityCode);

        if (userEmails.isEmpty()) {
            logger.warn("No emails found for {}", localAuthorityCode);
        }

        return userEmails;
    }
}
