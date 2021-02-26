package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.furtherevidence.FurtherEvidenceDocumentUploadedData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.FurtherEvidenceUploadedEmailContentProvider;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class FurtherEvidenceNotificationServiceTest {
    private static final String LOCAL_AUTHORITY = "LA";
    private static final Set<String> LOCAL_AUTHORITY_EMAILS = Set.of("la@example.com");
    private static final String REP_EMAIL = "rep@example.com";
    private static final String REP_2_EMAIL = "rep2@example.com";
    private static final String REP_3_EMAIL = "rep3@example.com";
    private static final Set<String> REP_EMAILS = Set.of(REP_EMAIL, REP_2_EMAIL, REP_3_EMAIL);
    private static final Long CASE_ID = 12345L;

    @Mock
    private InboxLookupService inboxLookupService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private FurtherEvidenceUploadedEmailContentProvider furtherEvidenceUploadedEmailContentProvider;

    @InjectMocks
    private FurtherEvidenceNotificationService furtherEvidenceNotificationService;

    @Test
    void shouldReturnLASolicitors() {
        CaseData caseData = caseData();

        when(inboxLookupService.getRecipients(LocalAuthorityInboxRecipientsRequest.builder()
            .caseData(caseData)
            .build()))
            .thenReturn(LOCAL_AUTHORITY_EMAILS);

        Set<String> actual = furtherEvidenceNotificationService.getLocalAuthoritySolicitorEmails(caseData);

        assertThat(actual).isEqualTo(LOCAL_AUTHORITY_EMAILS);
    }

    @Test
    void shouldReturnRespondentSolicitors() {
        CaseData caseData = caseData();

        Set<String> actual = furtherEvidenceNotificationService.getRespondentRepresentativeEmails(caseData);

        assertThat(actual).isEqualTo(REP_EMAILS);
    }

    @Test
    void shouldSendCorrectNotification() {
        CaseData caseData = caseData();

        Set<String> recipients = Set.of("test@example.com");

        FurtherEvidenceDocumentUploadedData furtherEvidenceDocumentUploadedData =
            FurtherEvidenceDocumentUploadedData.builder().build();

        when(furtherEvidenceUploadedEmailContentProvider.buildParameters(caseData, "Sender")).thenReturn(
            furtherEvidenceDocumentUploadedData);

        furtherEvidenceNotificationService.sendFurtherEvidenceDocumentsUploadedNotification(caseData,
            recipients,
            "Sender");

        verify(notificationService).sendEmail(FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE, recipients,
            furtherEvidenceDocumentUploadedData, CASE_ID.toString());
    }

    @Test
    void shouldNotReturnRecipientsWhenCaseHasNotRepresentatives() {
        CaseData emptyCaseData = CaseData.builder().build();
        Set<String> actual = furtherEvidenceNotificationService.getRespondentRepresentativeEmails(emptyCaseData);
        assertTrue(actual.isEmpty());
    }

    @Test
    void shouldNotSendNotificationWhenNoRecipientsAreProvided() {
        CaseData caseData = caseData();

        Set<String> recipients = Set.of();

        furtherEvidenceNotificationService.sendFurtherEvidenceDocumentsUploadedNotification(caseData,
            recipients,
            "Sender");

        verifyNoInteractions(notificationService);
    }

    CaseData caseData() {
        Representative representative = Representative
            .builder()
            .email(REP_EMAIL)
            .role(RepresentativeRole.REPRESENTING_RESPONDENT_1)
            .servingPreferences(RepresentativeServingPreferences.DIGITAL_SERVICE)
            .build();

        Representative representative2 = Representative
            .builder()
            .email(REP_2_EMAIL)
            .role(RepresentativeRole.REPRESENTING_RESPONDENT_2)
            .servingPreferences(RepresentativeServingPreferences.DIGITAL_SERVICE)
            .build();

        Representative representative3 = Representative
            .builder()
            .email(REP_3_EMAIL)
            .role(RepresentativeRole.REPRESENTING_RESPONDENT_2)
            .servingPreferences(RepresentativeServingPreferences.DIGITAL_SERVICE)
            .build();

        Representative unrelatedRepresentative = Representative
            .builder()
            .email("ignore@example.com")
            .role(RepresentativeRole.REPRESENTING_PERSON_1)
            .servingPreferences(RepresentativeServingPreferences.DIGITAL_SERVICE)
            .build();

        Representative emailRepresentative = Representative
            .builder()
            .email("ignore_email@example.com")
            .role(RepresentativeRole.REPRESENTING_PERSON_1)
            .servingPreferences(RepresentativeServingPreferences.EMAIL)
            .build();

        return CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY)
            .representatives(List.of(
                element(representative),
                element(representative2),
                element(representative3),
                element(unrelatedRepresentative),
                element(emailRepresentative)))
            .build();
    }
}
