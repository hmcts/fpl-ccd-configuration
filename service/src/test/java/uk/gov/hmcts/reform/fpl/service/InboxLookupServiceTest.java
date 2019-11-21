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
import uk.gov.hmcts.reform.fpl.config.PublicLawEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.Solicitor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, LocalAuthorityEmailLookupConfiguration.class,
    InboxLookupService.class, PublicLawEmailLookupConfiguration.class})
public class InboxLookupServiceTest {

    @MockBean
    private LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration;

    @MockBean
    private PublicLawEmailLookupConfiguration publicLawEmailLookupConfiguration;

    @Autowired
    private ObjectMapper mapper;

    private InboxLookupService inboxLookupService;

    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "FamilyPublicLaw+sa@gmail.com";
    private static final String SOLICITOR_EMAIL_ADDRESS = "FamilyPublicLaw+sa@gmail.com";
    private static final String PUBLIC_LAW_EMAIL = "FamilyPublicLaw+PublicLawEmail@gmail.com";

    @BeforeEach
    void setup() {
        this.inboxLookupService =
            new InboxLookupService(mapper,
                localAuthorityEmailLookupConfiguration,
                publicLawEmailLookupConfiguration);
    }

    @Test
    void shouldReturnLocalAuthorityEmailWhenEmailExist() {
        CaseDetails caseDetails = buildCaseDetailsWithSolicitorEmail();

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(new LocalAuthorityEmailLookupConfiguration.LocalAuthority(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        String email = inboxLookupService.getNotificationRecipientEmail(caseDetails, LOCAL_AUTHORITY_CODE);

        assertThat(email).isEqualTo(LOCAL_AUTHORITY_EMAIL_ADDRESS);
    }

    @Test
    void shouldReturnSolicitorEmailWhenLocalAuthorityEmailDoesNotExist() {
        CaseDetails caseDetails = buildCaseDetailsWithSolicitorEmail();

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(new LocalAuthorityEmailLookupConfiguration.LocalAuthority(null));

        String email = inboxLookupService.getNotificationRecipientEmail(caseDetails, LOCAL_AUTHORITY_CODE);

        assertThat(email).isEqualTo(SOLICITOR_EMAIL_ADDRESS);
    }

    @Test
    void shouldReturnSolicitorEmailWhenLocalAuthorityEmailIsEmpty() {
        CaseDetails caseDetails = buildCaseDetailsWithSolicitorEmail();

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(new LocalAuthorityEmailLookupConfiguration.LocalAuthority(""));

        String email = inboxLookupService.getNotificationRecipientEmail(caseDetails, LOCAL_AUTHORITY_CODE);

        assertThat(email).isEqualTo(SOLICITOR_EMAIL_ADDRESS);
    }

    @Test
    void shouldReturnPublicLawEmailWhenLocalAuthorityEmailAndSolicitorEmailIsEmpty() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of("solicitor", Solicitor.builder().email("").build()))
            .build();

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(new LocalAuthorityEmailLookupConfiguration.LocalAuthority(""));

        given(publicLawEmailLookupConfiguration.getEmailAddress())
            .willReturn(PUBLIC_LAW_EMAIL);

        String email = inboxLookupService.getNotificationRecipientEmail(caseDetails, LOCAL_AUTHORITY_CODE);

        assertThat(email).isEqualTo(PUBLIC_LAW_EMAIL);
    }

    private CaseDetails buildCaseDetailsWithSolicitorEmail() {
        return CaseDetails.builder()
            .data(ImmutableMap.of("solicitor", Solicitor.builder().email(SOLICITOR_EMAIL_ADDRESS).build()))
            .build();
    }
}
