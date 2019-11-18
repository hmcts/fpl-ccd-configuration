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
import uk.gov.hmcts.reform.fpl.config.GeneralFplaEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.Solicitor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, LocalAuthorityEmailLookupConfiguration.class,
    GenericInboxLookupService.class, GeneralFplaEmailLookupConfiguration.class})
public class GenericInboxLookupServiceTest {

    @MockBean
    private LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration;

    @MockBean
    private GeneralFplaEmailLookupConfiguration generalFplaEmailLookupConfiguration;

    @Autowired
    private ObjectMapper mapper;

    private GenericInboxLookupService genericInboxLookupService;

    private CaseDetails caseDetails;

    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "FamilyPublicLaw+sa@gmail.com";
    private static final String SOLICITOR_EMAIL_ADDRESS = "FamilyPublicLaw+sa@gmail.com";
    private static final String GENERAL_FPLA_INBOX = "FamilyPublicLaw+generalInbox@gmail.com";

    @BeforeEach
    void setup() {
        this.genericInboxLookupService =
            new GenericInboxLookupService(mapper,
                localAuthorityEmailLookupConfiguration,
                generalFplaEmailLookupConfiguration);
    }

    @Test
    void shouldReturnLocalAuthorityInboxWhenMappingExist() {
        buildCaseDetails(SOLICITOR_EMAIL_ADDRESS);
        getMockLocalAuthorityEmail(LOCAL_AUTHORITY_EMAIL_ADDRESS);

        String email = genericInboxLookupService.getEmail(caseDetails, LOCAL_AUTHORITY_CODE);

        assertThat(email).isEqualTo(LOCAL_AUTHORITY_EMAIL_ADDRESS);
    }

    @Test
    void shouldReturnSolicitorEmailWhenLocalAuthorityInboxNotExist() {
        buildCaseDetails(SOLICITOR_EMAIL_ADDRESS);
        getMockLocalAuthorityEmail("");

        String email = genericInboxLookupService.getEmail(caseDetails, LOCAL_AUTHORITY_CODE);

        assertThat(email).isEqualTo(SOLICITOR_EMAIL_ADDRESS);
    }

    @Test
    void shouldReturnGeneralInboxWhenSolicitorEmailNotExist() {
        buildCaseDetails("");
        getMockLocalAuthorityEmail("");

        given(generalFplaEmailLookupConfiguration.getGeneralFplaInbox())
            .willReturn(GENERAL_FPLA_INBOX);

        String email = genericInboxLookupService.getEmail(caseDetails, LOCAL_AUTHORITY_CODE);

        assertThat(email).isEqualTo(GENERAL_FPLA_INBOX);
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
