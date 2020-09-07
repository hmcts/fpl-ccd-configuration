package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Solicitor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration.LocalAuthority;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LocalAuthorityEmailLookupConfiguration.class, InboxLookupService.class})
class InboxLookupServiceTest {
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "FamilyPublicLaw+sa@gmail.com";
    private static final String SOLICITOR_EMAIL_ADDRESS = "FamilyPublicLaw+solicitor@gmail.com";
    private static final String FALLBACK_INBOX = "FamilyPublicLaw@gmail.com";

    @MockBean
    private LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration;

    private InboxLookupService inboxLookupService;

    @BeforeEach
    void setup() {
        this.inboxLookupService = new InboxLookupService(localAuthorityEmailLookupConfiguration, FALLBACK_INBOX);
    }

    @Test
    void shouldReturnLocalAuthorityEmailWhenLocalAuthorityEmailExist() {
        CaseData caseData = buildCaseDetails(LOCAL_AUTHORITY_CODE);

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.of(new LocalAuthority(LOCAL_AUTHORITY_EMAIL_ADDRESS)));

        String email = inboxLookupService.getNotificationRecipientEmail(caseData);

        assertThat(email).isEqualTo(LOCAL_AUTHORITY_EMAIL_ADDRESS);
    }

    @Test
    void shouldReturnSolicitorEmailWhenLocalAuthorityEmailMappingNotExist() {
        CaseData caseData = buildCaseDetails(LOCAL_AUTHORITY_CODE);

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.empty());

        String email = inboxLookupService.getNotificationRecipientEmail(caseData);

        assertThat(email).isEqualTo(SOLICITOR_EMAIL_ADDRESS);
    }

    @Test
    void shouldReturnSolicitorEmailWhenLocalAuthorityEmailIsEmpty() {
        CaseData caseData = buildCaseDetails(LOCAL_AUTHORITY_CODE);

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.of(new LocalAuthority("")));

        String email = inboxLookupService.getNotificationRecipientEmail(caseData);

        assertThat(email).isEqualTo(SOLICITOR_EMAIL_ADDRESS);
    }

    @Test
    void shouldReturnPublicLawEmailWhenLocalAuthorityEmailAndSolicitorEmailIsEmpty() {
        CaseData caseData = CaseData.builder()
            .solicitor(Solicitor.builder().email("").build())
            .build();

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.of(new LocalAuthority("")));

        String email = inboxLookupService.getNotificationRecipientEmail(caseData);

        assertThat(email).isEqualTo(FALLBACK_INBOX);
    }

    @Test
    void shouldReturnPublicLawEmailWhenLocalAuthorityEmailIsNorPresentAndSolicitorIsNotPresent() {
        CaseData caseData = CaseData.builder().build();

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.empty());

        String email = inboxLookupService.getNotificationRecipientEmail(caseData);

        assertThat(email).isEqualTo(FALLBACK_INBOX);
    }

    private CaseData buildCaseDetails(String localAuthority) {
        return CaseData.builder()
            .id(RandomUtils.nextLong())
            .caseLocalAuthority(localAuthority)
            .solicitor(Solicitor.builder().email(SOLICITOR_EMAIL_ADDRESS).build())
            .build();
    }
}
