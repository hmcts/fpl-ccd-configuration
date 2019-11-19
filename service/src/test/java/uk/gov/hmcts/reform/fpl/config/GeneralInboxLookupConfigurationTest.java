package uk.gov.hmcts.reform.fpl.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeneralInboxLookupConfigurationTest {

    private static final String GENERAL_INBOX = "FamilyPublicLaw+generalInbox@gmail.com";

    private GeneralInboxLookupConfiguration generalInboxLookupConfiguration
        = new GeneralInboxLookupConfiguration(GENERAL_INBOX);

    @Test
    void shouldReturnGeneralEmail() {
        String generalInbox = generalInboxLookupConfiguration.getGeneralInbox();

        assertThat(generalInbox).isEqualTo(GENERAL_INBOX);
    }
}
