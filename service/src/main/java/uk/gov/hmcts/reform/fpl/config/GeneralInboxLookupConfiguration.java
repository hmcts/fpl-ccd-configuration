package uk.gov.hmcts.reform.fpl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@lombok.Data
@Service
public class GeneralInboxLookupConfiguration {

    private final String generalInbox;

    public GeneralInboxLookupConfiguration(@Value("${fpl.general_fpla_inbox.mapping}") String generalInbox) {
        this.generalInbox = generalInbox;
    }
}
