package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.GeneralEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.Solicitor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, LocalAuthorityEmailLookupConfiguration.class,
    InboxLookupService.class, GeneralEmailLookupConfiguration.class})
public class InboxLookupServiceTest {

    @MockBean
    private LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration;

    @MockBean
    private GeneralEmailLookupConfiguration generalEmailLookupConfiguration;

    @Autowired
    private ObjectMapper mapper;

    private InboxLookupService inboxLookupService;

    private CaseDetails caseDetails;

    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "FamilyPublicLaw+sa@gmail.com";
    private static final String SOLICITOR_EMAIL_ADDRESS = "FamilyPublicLaw+sa@gmail.com";
    private static final String GENERAL_INBOX = "FamilyPublicLaw+generalInbox@gmail.com";

    @BeforeEach
    void setup() {
        this.inboxLookupService =
            new InboxLookupService(mapper,
                localAuthorityEmailLookupConfiguration,
                generalEmailLookupConfiguration);
    }

    @Test
    void shouldReturnLocalAuthorityInboxWhenMappingExist() {
        buildCaseDetails(SOLICITOR_EMAIL_ADDRESS);
        getMockLocalAuthorityEmail(LOCAL_AUTHORITY_EMAIL_ADDRESS);

        String email = inboxLookupService.getLocalAuthorityOrFallbackEmail(caseDetails, LOCAL_AUTHORITY_CODE);

        assertThat(email).isEqualTo(LOCAL_AUTHORITY_EMAIL_ADDRESS);
    }

    @Test
    void shouldReturnSolicitorEmailWhenLocalAuthorityInboxNotExist() {
        buildCaseDetails(SOLICITOR_EMAIL_ADDRESS);
        getMockLocalAuthorityEmail("");

        String email = inboxLookupService.getLocalAuthorityOrFallbackEmail(caseDetails, LOCAL_AUTHORITY_CODE);

        assertThat(email).isEqualTo(SOLICITOR_EMAIL_ADDRESS);
    }

    @Test
    void shouldReturnGeneralInboxWhenSolicitorEmailNotExist() {
        buildCaseDetails("");
        getMockLocalAuthorityEmail("");

        given(generalEmailLookupConfiguration.getGeneralInbox())
            .willReturn(GENERAL_INBOX);

        String email = inboxLookupService.getLocalAuthorityOrFallbackEmail(caseDetails, LOCAL_AUTHORITY_CODE);

        assertThat(email).isEqualTo(GENERAL_INBOX);
    }

    private void getMockLocalAuthorityEmail(String localAuthorityEmailAddress) {
        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(new LocalAuthorityEmailLookupConfiguration.LocalAuthority(localAuthorityEmailAddress));
    }

    private void buildCaseDetails(String email) {
        caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of("solicitor", Solicitor.builder().email(email).build()))
            .build();
    }
}
