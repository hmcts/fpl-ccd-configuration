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
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.service.InboxLookupServiceTest.FALLBACK_INBOX;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LocalAuthorityEmailLookupConfiguration.class, InboxLookupService.class})
@TestPropertySource(properties = {"fpl.local_authority_fallback_inbox=" + FALLBACK_INBOX})
class InboxLookupServiceTest {
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "FamilyPublicLaw+sa@gmail.com";
    private static final String ADDITIONAL_EMAIL_1 = "solicitor@gmail.com";
    private static final String ADDITIONAL_EMAIL_2 = "caseworker@gmail.com";
    static final String FALLBACK_INBOX = "FamilyPublicLaw@gmail.com";
    private static final String LEGAL_REPRESENTATIVE_1_EMAIL = "legal.representative.1@solicitors.com";
    private static final String LEGAL_REPRESENTATIVE_2_EMAIL = "legal.representative.2@solicitors.com";
    private static final List<Element<LegalRepresentative>> LEGAL_REPRESENTATIVES = wrapElements(List.of(
        LegalRepresentative.builder().email(LEGAL_REPRESENTATIVE_1_EMAIL).build(),
        LegalRepresentative.builder().email(LEGAL_REPRESENTATIVE_2_EMAIL).build()));

    @MockBean
    private LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private ApplicantLocalAuthorityService localAuthorityService;

    @Autowired
    private InboxLookupService underTest;

    @Test
    void shouldReturnLocalAuthorityEmailWhenLocalAuthorityEmailExist() {
        CaseData caseData = buildCaseDetails(LOCAL_AUTHORITY_CODE);

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        Collection<String> emails = underTest.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build());

        assertThat(emails).containsExactly(LOCAL_AUTHORITY_EMAIL_ADDRESS);
    }

    @Test
    void shouldReturnLAAndLegalReprEmailWhenLocalAuthorityEmailExistAndLegalRepresentativesIfExistAndNotExcluded() {
        CaseData caseData = buildCaseDetails(LOCAL_AUTHORITY_CODE).toBuilder()
            .legalRepresentatives(LEGAL_REPRESENTATIVES).build();

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        Collection<String> emails = underTest.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder()
                .caseData(caseData)
                .excludeLegalRepresentatives(false)
                .build());

        assertThat(emails).containsExactly(
            LOCAL_AUTHORITY_EMAIL_ADDRESS,
            LEGAL_REPRESENTATIVE_1_EMAIL,
            LEGAL_REPRESENTATIVE_2_EMAIL
        );
    }

    @Test
    void shouldReturnOnlyLocalAuthorityEmailWhenLocalAuthorityEmailExistAndLegalRepresentativesIfExistButExcluded() {
        CaseData caseData = buildCaseDetails(LOCAL_AUTHORITY_CODE).toBuilder()
            .legalRepresentatives(LEGAL_REPRESENTATIVES).build();

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        Collection<String> emails = underTest.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder()
                .caseData(caseData)
                .excludeLegalRepresentatives(true)
                .build());

        assertThat(emails).containsExactly(LOCAL_AUTHORITY_EMAIL_ADDRESS);
    }

    @Test
    void shouldReturnOnlyLAEmailWhenLAEmailExistAndLegalRepresentativesEmptyCollectionAndNotExcluded() {
        CaseData caseData = buildCaseDetails(LOCAL_AUTHORITY_CODE).toBuilder()
            .legalRepresentatives(emptyList())
            .build();
        ;

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        Collection<String> emails = underTest.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder()
                .caseData(caseData)
                .excludeLegalRepresentatives(false)
                .build());

        assertThat(emails).containsExactly(LOCAL_AUTHORITY_EMAIL_ADDRESS);
    }

    @Test
    void shouldReturnOnlyLAEmailWhenLAEmailExistAndLegalRepresentativesEmptyAndNotExcluded() {
        CaseData caseData = buildCaseDetails(LOCAL_AUTHORITY_CODE);

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        Collection<String> emails = underTest.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder()
                .caseData(caseData)
                .excludeLegalRepresentatives(false)
                .build());

        assertThat(emails).containsExactly(LOCAL_AUTHORITY_EMAIL_ADDRESS);
    }

    @Test
    void shouldReturnSolicitorEmailWhenLocalAuthorityEmailMappingNotExist() {
        CaseData caseData = buildCaseDetails(LOCAL_AUTHORITY_CODE);

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.empty());

        given(localAuthorityService.getContactsEmails(caseData))
            .willReturn(List.of(ADDITIONAL_EMAIL_1, ADDITIONAL_EMAIL_2));

        Collection<String> emails = underTest.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build());

        assertThat(emails).containsExactlyInAnyOrder(ADDITIONAL_EMAIL_1, ADDITIONAL_EMAIL_2);
    }

    @Test
    void shouldReturnSolicitorEmailWhenLocalAuthorityEmailIsEmpty() {
        CaseData caseData = buildCaseDetails(LOCAL_AUTHORITY_CODE);

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.of(""));

        given(localAuthorityService.getContactsEmails(caseData))
            .willReturn(List.of(ADDITIONAL_EMAIL_1, ADDITIONAL_EMAIL_2));

        Collection<String> emails = underTest.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build());

        assertThat(emails).containsExactlyInAnyOrder(ADDITIONAL_EMAIL_1, ADDITIONAL_EMAIL_2);
    }

    @Test
    void shouldReturnLocalAuthorityEmailAndAdditionalContactsWhenFeatureToggleEnabled() {
        CaseData caseData = buildCaseDetails(LOCAL_AUTHORITY_CODE);

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(featureToggleService.isSendLAEmailsToSolicitorEnabled(LOCAL_AUTHORITY_CODE)).willReturn(true);
        given(localAuthorityService.getContactsEmails(caseData))
            .willReturn(List.of(ADDITIONAL_EMAIL_1, ADDITIONAL_EMAIL_2));

        Collection<String> emails = underTest.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build());

        assertThat(emails).containsExactlyInAnyOrder(
            LOCAL_AUTHORITY_EMAIL_ADDRESS,
            ADDITIONAL_EMAIL_1,
            ADDITIONAL_EMAIL_2);
    }

    @Test
    void shouldReturnPublicLawEmailWhenLocalAuthorityEmailAndSolicitorEmailAndLegalRepresentativesEmailsIsEmpty() {
        CaseData caseData = CaseData.builder()
            .solicitor(Solicitor.builder().email("").build())
            .build();

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.of(""));

        Collection<String> emails = underTest.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build());

        assertThat(emails).containsExactly(FALLBACK_INBOX);
    }

    @Test
    void shouldReturnFallbackWhenLAEmailIsNorPresentAndSolicitorIsNotPresentAndLegalRepsEmailAreNotPresent() {
        CaseData caseData = CaseData.builder().build();

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.empty());

        Collection<String> emails = underTest.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build());

        assertThat(emails).containsExactly(FALLBACK_INBOX);
    }

    private CaseData buildCaseDetails(String localAuthority) {
        return CaseData.builder()
            .id(RandomUtils.nextLong())
            .caseLocalAuthority(localAuthority)
            .build();
    }
}
