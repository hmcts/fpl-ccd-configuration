package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.furtherevidence.FurtherEvidenceDocumentUploadedData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.FurtherEvidenceUploadedEmailContentProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.CAFCASS_SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_PERSON_1;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_1;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_2;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class FurtherEvidenceNotificationServiceTest {
    private static final String LOCAL_AUTHORITY = "LA";
    private static final Set<String> LOCAL_AUTHORITY_EMAILS = Set.of("la@example.com");
    private static final Long CASE_ID = 12345L;

    private static final Representative REPRESENTATIVE_WITH_SERVICE_ACCESS_1 = Representative.builder()
        .email("rep@example.com")
        .role(REPRESENTING_RESPONDENT_1)
        .servingPreferences(DIGITAL_SERVICE)
        .build();

    private static final Representative REPRESENTATIVE_WITH_SERVICE_ACCESS_2 = Representative.builder()
        .email("rep2@example.com")
        .role(REPRESENTING_RESPONDENT_2)
        .servingPreferences(DIGITAL_SERVICE)
        .build();

    private static final Representative REPRESENTATIVE_WITH_SERVICE_ACCESS_3 = Representative
        .builder()
        .email("rep3@example.com")
        .role(REPRESENTING_RESPONDENT_2)
        .servingPreferences(DIGITAL_SERVICE)
        .build();

    private static final Representative UNRELATED_REPRESENTATIVE = Representative
        .builder()
        .email("ignore@example.com")
        .role(REPRESENTING_PERSON_1)
        .servingPreferences(DIGITAL_SERVICE)
        .build();

    private static final Representative REPRESENTATIVE_SERVED_BY_EMAIL = Representative
        .builder()
        .email("ignore_email@example.com")
        .role(REPRESENTING_PERSON_1)
        .servingPreferences(EMAIL)
        .build();

    private static final Representative CAFCASS_SOLICITOR_SERVED_BY_EMAIL = Representative
        .builder()
        .email("cafcass1@example.com")
        .role(CAFCASS_SOLICITOR)
        .servingPreferences(EMAIL)
        .build();

    private static final Representative CAFCASS_SOLICITOR_WITH_SERVICE_ACCESS = Representative
        .builder()
        .email("cafcass2@example.com")
        .role(CAFCASS_SOLICITOR)
        .servingPreferences(DIGITAL_SERVICE)
        .build();

    private static final List<String> DOCUMENTS = buildDocumentsNamesList();

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
    void shouldReturnRespondentRepresentativesAndCafcassSolicitorsWithServiceAccess() {
        CaseData caseData = caseData(
            REPRESENTATIVE_WITH_SERVICE_ACCESS_1,
            REPRESENTATIVE_WITH_SERVICE_ACCESS_2,
            REPRESENTATIVE_WITH_SERVICE_ACCESS_3,
            UNRELATED_REPRESENTATIVE,
            REPRESENTATIVE_SERVED_BY_EMAIL,
            CAFCASS_SOLICITOR_SERVED_BY_EMAIL,
            CAFCASS_SOLICITOR_WITH_SERVICE_ACCESS);

        Set<String> actualRecipients = furtherEvidenceNotificationService.getRepresentativeEmails(caseData);

        assertThat(actualRecipients).containsExactlyInAnyOrder(
            REPRESENTATIVE_WITH_SERVICE_ACCESS_1.getEmail(),
            REPRESENTATIVE_WITH_SERVICE_ACCESS_2.getEmail(),
            REPRESENTATIVE_WITH_SERVICE_ACCESS_3.getEmail(),
            CAFCASS_SOLICITOR_WITH_SERVICE_ACCESS.getEmail());
    }

    @Test
    void shouldReturnEmptyRecipientsWhenCaseHasNotRepresentatives() {
        CaseData emptyCaseData = caseData();

        Set<String> actualRecipients = furtherEvidenceNotificationService.getRepresentativeEmails(emptyCaseData);

        assertThat(actualRecipients).isEmpty();
    }

    @Test
    void shouldReturnEmptyRecipientsWhenCaseHasNotRespondentRepresentativesServedByPost() {
        CaseData emptyCaseData = caseData(
            UNRELATED_REPRESENTATIVE,
            REPRESENTATIVE_SERVED_BY_EMAIL,
            CAFCASS_SOLICITOR_SERVED_BY_EMAIL);

        Set<String> actualRecipients = furtherEvidenceNotificationService.getRepresentativeEmails(emptyCaseData);

        assertThat(actualRecipients).isEmpty();
    }

    @Test
    void shouldSendCorrectNotification() {
        CaseData caseData = caseData();

        Set<String> recipients = Set.of("test@example.com");

        FurtherEvidenceDocumentUploadedData furtherEvidenceDocumentUploadedData =
            FurtherEvidenceDocumentUploadedData.builder().build();

        when(furtherEvidenceUploadedEmailContentProvider.buildParameters(caseData, "Sender",
            DOCUMENTS)).thenReturn(
            furtherEvidenceDocumentUploadedData);

        furtherEvidenceNotificationService.sendNotification(caseData, recipients, "Sender", DOCUMENTS);

        verify(notificationService).sendEmail(FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE, recipients,
            furtherEvidenceDocumentUploadedData, CASE_ID.toString());
    }

    @Test
    void shouldNotSendNotificationWhenNoRecipientsAreProvided() {
        CaseData caseData = caseData();

        Set<String> recipients = Set.of();

        furtherEvidenceNotificationService.sendNotification(caseData, recipients, "Sender", DOCUMENTS);

        verifyNoInteractions(notificationService);
    }

    private CaseData caseData(Representative... representatives) {
        return CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY)
            .representatives(wrapElements(representatives))
            .build();
    }

    private static List<String> buildDocumentsNamesList() {
        List<String> documents = new ArrayList<>();
        documents.add("DOCUMENT 1");
        documents.add("DOCUMENT 2");
        return documents;
    }
}
