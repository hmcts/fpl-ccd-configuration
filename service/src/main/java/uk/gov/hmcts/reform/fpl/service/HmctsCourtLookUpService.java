package uk.gov.hmcts.reform.fpl.service;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration.Court;

import static com.google.common.base.Preconditions.checkNotNull;

@Service
public class HmctsCourtLookUpService {

    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public HmctsCourtLookUpService(HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration) {
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
    }

    public Court getCourt(String localAuthorityCode) {
        checkNotNull(localAuthorityCode, "Case does not have local authority assigned");

        Court court = hmctsCourtLookupConfiguration.getLookupTable().get(localAuthorityCode);

        checkNotNull(court, "Court information not found");

        if (Strings.isNullOrEmpty(court.getEmail())) {
            logger.warn("No court email found for {}", localAuthorityCode);
        }

        if (Strings.isNullOrEmpty(court.getName())) {
            logger.warn("No court name found for {}", localAuthorityCode);
        }

        return court;
    }
}
