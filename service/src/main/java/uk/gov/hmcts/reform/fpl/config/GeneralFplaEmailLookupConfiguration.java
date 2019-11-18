package uk.gov.hmcts.reform.fpl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@lombok.Data
@Service
public class GeneralFplaEmailLookupConfiguration {

    private final String generalFplaInbox;

    public GeneralFplaEmailLookupConfiguration(@Value("${fpl.general_fpla_inbox.mapping}") String email) {
        this.generalFplaInbox = email;
    }
}
