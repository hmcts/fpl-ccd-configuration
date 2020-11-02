package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Solicitor;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration.LocalAuthority;
import static uk.gov.hmcts.reform.fpl.service.InboxLookupServiceTest.FALLBACK_INBOX;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LocalAuthorityEmailLookupConfiguration.class, InboxLookupService.class})
@TestPropertySource(properties = {"fpl.local_authority_fallback_inbox=" + FALLBACK_INBOX})
class InboxLookupServiceTest {
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "FamilyPublicLaw+sa@gmail.com";
    private static final String SOLICITOR_EMAIL_ADDRESS = "FamilyPublicLaw+solicitor@gmail.com";
    static final String FALLBACK_INBOX = "FamilyPublicLaw@gmail.com";

    @MockBean
    private LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private InboxLookupService inboxLookupService;

    @Test
    void shouldReturnLocalAuthorityEmailWhenLocalAuthorityEmailExist() {
        CaseData caseData = buildCaseDetails(LOCAL_AUTHORITY_CODE);

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.of(new LocalAuthority(LOCAL_AUTHORITY_EMAIL_ADDRESS)));

        Collection<String> emails = inboxLookupService.getRecipients(caseData);

        assertThat(emails).containsExactly(LOCAL_AUTHORITY_EMAIL_ADDRESS);
    }

    @Test
    void shouldReturnSolicitorEmailWhenLocalAuthorityEmailMappingNotExist() {
        CaseData caseData = buildCaseDetails(LOCAL_AUTHORITY_CODE);

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.empty());

        Collection<String> emails = inboxLookupService.getRecipients(caseData);

        assertThat(emails).containsExactly(SOLICITOR_EMAIL_ADDRESS);
    }

    @Test
    void shouldReturnSolicitorEmailWhenLocalAuthorityEmailIsEmpty() {
        CaseData caseData = buildCaseDetails(LOCAL_AUTHORITY_CODE);

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.of(new LocalAuthority("")));

        Collection<String> emails = inboxLookupService.getRecipients(caseData);

        assertThat(emails).containsExactly(SOLICITOR_EMAIL_ADDRESS);
    }

    @Test
    void shouldReturnLocalAuthorityEmailAndSolicitorEmailWhenFeatureToggleEnabled() {
        CaseData caseData = buildCaseDetails(LOCAL_AUTHORITY_CODE);

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.of(new LocalAuthority(LOCAL_AUTHORITY_EMAIL_ADDRESS)));

        given(featureToggleService.isSendLAEmailsToSolicitorEnabled(LOCAL_AUTHORITY_CODE)).willReturn(true);

        Collection<String> emails = inboxLookupService.getRecipients(caseData);

        assertThat(emails).containsExactlyInAnyOrder(LOCAL_AUTHORITY_EMAIL_ADDRESS, SOLICITOR_EMAIL_ADDRESS);
    }

    @Test
    void shouldReturnPublicLawEmailWhenLocalAuthorityEmailAndSolicitorEmailIsEmpty() {
        CaseData caseData = CaseData.builder()
            .solicitor(Solicitor.builder().email("").build())
            .build();

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.of(new LocalAuthority("")));

        Collection<String> emails = inboxLookupService.getRecipients(caseData);

        assertThat(emails).containsExactly(FALLBACK_INBOX);
    }

    @Test
    void shouldReturnPublicLawEmailWhenLocalAuthorityEmailIsNorPresentAndSolicitorIsNotPresent() {
        CaseData caseData = CaseData.builder().build();

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.empty());

        Collection<String> emails = inboxLookupService.getRecipients(caseData);

        assertThat(emails).containsExactly(FALLBACK_INBOX);
    }

    private CaseData buildCaseDetails(String localAuthority) {
        return CaseData.builder()
            .id(RandomUtils.nextLong())
            .caseLocalAuthority(localAuthority)
            .solicitor(Solicitor.builder().email(SOLICITOR_EMAIL_ADDRESS).build())
            .build();
    }
}
