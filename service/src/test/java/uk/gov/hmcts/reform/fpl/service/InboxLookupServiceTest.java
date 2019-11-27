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
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.Solicitor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration.LocalAuthority;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, LocalAuthorityEmailLookupConfiguration.class,
    InboxLookupService.class})
class InboxLookupServiceTest {
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "FamilyPublicLaw+sa@gmail.com";
    private static final String SOLICITOR_EMAIL_ADDRESS = "FamilyPublicLaw+solicitor@gmail.com";
    private static final String FALLBACK_INBOX = "FamilyPublicLaw@gmail.com";

    @MockBean
    private LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration;

    @Autowired
    private ObjectMapper mapper;

    private InboxLookupService inboxLookupService;

    @BeforeEach
    void setup() {
        this.inboxLookupService = new InboxLookupService(
            mapper,
            localAuthorityEmailLookupConfiguration,
            FALLBACK_INBOX);
    }

    @Test
    void shouldReturnLocalAuthorityEmailWhenLocalAuthorityEmailExist() {
        CaseDetails caseDetails = buildCaseDetails();

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.of(new LocalAuthority(LOCAL_AUTHORITY_EMAIL_ADDRESS)));

        String email = inboxLookupService.getNotificationRecipientEmail(caseDetails, LOCAL_AUTHORITY_CODE);

        assertThat(email).isEqualTo(LOCAL_AUTHORITY_EMAIL_ADDRESS);
    }

    @Test
    void shouldReturnSolicitorEmailWhenLocalAuthorityEmailMappingNotExist() {
        CaseDetails caseDetails = buildCaseDetails();

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.empty());

        String email = inboxLookupService.getNotificationRecipientEmail(caseDetails, LOCAL_AUTHORITY_CODE);

        assertThat(email).isEqualTo(SOLICITOR_EMAIL_ADDRESS);
    }

    @Test
    void shouldReturnSolicitorEmailWhenLocalAuthorityEmailIsEmpty() {
        CaseDetails caseDetails = buildCaseDetails();

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.of(new LocalAuthority("")));

        String email = inboxLookupService.getNotificationRecipientEmail(caseDetails, LOCAL_AUTHORITY_CODE);

        assertThat(email).isEqualTo(SOLICITOR_EMAIL_ADDRESS);
    }

    @Test
    void shouldReturnPublicLawEmailWhenLocalAuthorityEmailAndSolicitorEmailIsEmpty() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of("solicitor", Solicitor.builder().email("").build()))
            .build();

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.of(new LocalAuthority("")));

        String email = inboxLookupService.getNotificationRecipientEmail(caseDetails, LOCAL_AUTHORITY_CODE);

        assertThat(email).isEqualTo(FALLBACK_INBOX);
    }

    private CaseDetails buildCaseDetails() {
        return CaseDetails.builder()
            .data(ImmutableMap.of("solicitor", Solicitor.builder().email(SOLICITOR_EMAIL_ADDRESS).build()))
            .build();
    }
}
