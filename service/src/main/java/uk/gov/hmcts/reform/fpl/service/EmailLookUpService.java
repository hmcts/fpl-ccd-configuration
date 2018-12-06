package uk.gov.hmcts.reform.fpl.service;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.UserEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.UserEmailLookupConfiguration.Court;

import static com.google.common.base.Preconditions.checkNotNull;

@Service
public class EmailLookUpService {

    private final UserEmailLookupConfiguration userEmailLookupConfiguration;
    private final Logger logger = LoggerFactory.getLogger(getClass());


    @Autowired
    public EmailLookUpService(UserEmailLookupConfiguration userEmailLookupConfiguration) {
        this.userEmailLookupConfiguration = userEmailLookupConfiguration;
    }

    public String getEmail(String localAuthorityCode) {
        checkNotNull(localAuthorityCode, "Case does not have local authority assigned");

        Court court = userEmailLookupConfiguration.getLookupTable().get(localAuthorityCode);

        if (Strings.isNullOrEmpty(court.getEmail())) {
            logger.warn("No email found for {}", localAuthorityCode);
        }

        return court.getEmail();
    }
}
