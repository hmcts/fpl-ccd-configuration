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
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.furtherevidence.FurtherEvidenceDocumentUploadedData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.FurtherEvidenceUploadedEmailContentProvider;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class FurtherEvidenceNotificationServiceTest {
    private static final String LOCAL_AUTHORITY = "LA";
    private static final Set<String> LOCAL_AUTHORITY_EMAILS = Set.of("la@example.com");
    private static final String REP_EMAIL = "rep@example.com";
    private static final Set<String> REP_EMAILS = Set.of(REP_EMAIL);
    private static final Long CASE_ID = 12345L;

    @Mock
    InboxLookupService inboxLookupService;
    @Mock
    NotificationService notificationService;
    @Mock
    FurtherEvidenceUploadedEmailContentProvider furtherEvidenceUploadedEmailContentProvider;

    @InjectMocks
    FurtherEvidenceNotificationService furtherEvidenceNotificationService;

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
    void shouldNotSendNotificationWhenNoRecipientsAreProvided() {
        CaseData caseData = caseData();

        Set<String> recipients = Set.of();

        FurtherEvidenceDocumentUploadedData furtherEvidenceDocumentUploadedData =
            FurtherEvidenceDocumentUploadedData.builder().build();

        furtherEvidenceNotificationService.sendFurtherEvidenceDocumentsUploadedNotification(caseData,
            recipients,
            "Sender");

        verify(notificationService, never()).sendEmail(any(), eq(recipients), any(), any());
    }

    CaseData caseData() {
        UUID representativeUUID = UUID.randomUUID();
        UUID unrealtedRepresentativeUUID = UUID.randomUUID();

        Representative representative = Representative
            .builder()
            .email(REP_EMAIL)
            .role(RepresentativeRole.REPRESENTING_RESPONDENT_1)
            .servingPreferences(RepresentativeServingPreferences.DIGITAL_SERVICE)
            .build();

        Representative unrelatedRepresentative = Representative
            .builder()
            .email("ignore@example.com")
            .role(RepresentativeRole.REPRESENTING_PERSON_1)
            .servingPreferences(RepresentativeServingPreferences.DIGITAL_SERVICE)
            .build();

        Respondent respondent = Respondent.builder()
            .representedBy(wrapElements(List.of(representativeUUID)))
            .build();

        Respondent respondent2 = Respondent.builder()
            .representedBy(wrapElements(List.of(unrealtedRepresentativeUUID)))
            .build();

        return CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY)
            .representatives(List.of(element(representativeUUID, representative),
                element(unrealtedRepresentativeUUID, unrelatedRepresentative)))
            .respondents1(wrapElements(respondent, respondent2))
            .build();
    }
}
