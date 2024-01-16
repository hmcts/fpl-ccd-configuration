package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.courtbundle.CourtBundleUploadedData;
import uk.gov.hmcts.reform.fpl.model.notify.furtherevidence.FurtherEvidenceDocumentUploadedData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.CourtBundleUploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.FurtherEvidenceUploadedEmailContentProvider;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.COURT_BUNDLE_UPLOADED_NOTIFICATION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.ADJOURNED;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.CAFCASS_SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_PERSON_1;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_1;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_2;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.Type.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.Type.RESPONDENT;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance.IN_PERSON;
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

    private static final Respondent RESPONDENT_1 = Respondent
        .builder()
        .party(RespondentParty.builder().lastName("Lastname").build())
        .representedBy(wrapElements(List.of(UUID.randomUUID())))
        .solicitor(RespondentSolicitor.builder().email("sol1@email.com").build())
        .build();

    private static final Child CHILD_1 = Child
        .builder()
        .party(ChildParty.builder().lastName("Lastname").build())
        .solicitor(RespondentSolicitor.builder().email("sol2@email.com").build())
        .build();

    private static final List<String> DOCUMENTS = buildDocumentsNamesList();

    @Mock
    private LocalAuthorityRecipientsService localAuthorityRecipients;

    @Mock
    private NotificationService notificationService;

    @Mock
    private FurtherEvidenceUploadedEmailContentProvider furtherEvidenceUploadedEmailContentProvider;

    @Mock
    private CourtBundleUploadedEmailContentProvider courtBundleUploadedEmailContentProvider;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private RepresentativesInbox representativesInbox;

    @InjectMocks
    private FurtherEvidenceNotificationService furtherEvidenceNotificationService;

    @Nested
    class LocalAuthoritiesRecipients {

        @Test
        void shouldReturnAllLocalAuthoritiesContacts() {
            final CaseData caseData = caseData();

            when(localAuthorityRecipients.getRecipients(any())).thenReturn(LOCAL_AUTHORITY_EMAILS);

            Set<String> actual = furtherEvidenceNotificationService.getLocalAuthoritiesRecipients(caseData);

            assertThat(actual).isEqualTo(LOCAL_AUTHORITY_EMAILS);

            verify(localAuthorityRecipients).getRecipients(RecipientsRequest.builder()
                .caseData(caseData)
                .build());
        }

        @Test
        void shouldReturnDesignatedLocalAuthoritiesContacts() {
            final CaseData caseData = caseData();

            when(localAuthorityRecipients.getRecipients(any())).thenReturn(LOCAL_AUTHORITY_EMAILS);

            Set<String> actual = furtherEvidenceNotificationService.getDesignatedLocalAuthorityRecipients(caseData);

            assertThat(actual).isEqualTo(LOCAL_AUTHORITY_EMAILS);

            verify(localAuthorityRecipients).getRecipients(RecipientsRequest.builder()
                .caseData(caseData)
                .secondaryLocalAuthorityExcluded(true)
                .build());
        }

        @Test
        void shouldReturnSecondaryLocalAuthoritiesContacts() {
            final CaseData caseData = caseData();

            when(localAuthorityRecipients.getRecipients(any())).thenReturn(LOCAL_AUTHORITY_EMAILS);

            Set<String> actual = furtherEvidenceNotificationService.getSecondaryLocalAuthorityRecipients(caseData);

            assertThat(actual).isEqualTo(LOCAL_AUTHORITY_EMAILS);

            verify(localAuthorityRecipients).getRecipients(RecipientsRequest.builder()
                .caseData(caseData)
                .designatedLocalAuthorityExcluded(true)
                .build());
        }
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

        when(representativesInbox.getRepresentativeEmailsFilteredByRole(caseData, DIGITAL_SERVICE,
            List.of(CAFCASS, RESPONDENT)
        ))
            .thenReturn(newHashSet("rep@example.com", "rep2@example.com", "rep3@example.com",
                "cafcass2@example.com"));
        Set<String> actualRecipients = furtherEvidenceNotificationService.getRepresentativeEmails(caseData);

        assertThat(actualRecipients).containsExactlyInAnyOrder(
            REPRESENTATIVE_WITH_SERVICE_ACCESS_1.getEmail(),
            REPRESENTATIVE_WITH_SERVICE_ACCESS_2.getEmail(),
            REPRESENTATIVE_WITH_SERVICE_ACCESS_3.getEmail(),
            CAFCASS_SOLICITOR_WITH_SERVICE_ACCESS.getEmail());
    }

    @Test
    void shouldReturnRespondentAndChildrenRepresentativesWhenSolicitorUser() {
        CaseData caseData = caseData(CHILD_1, RESPONDENT_1);

        when(representativesInbox.getRespondentSolicitorEmails(caseData, DIGITAL_SERVICE))
            .thenReturn(newHashSet("sol1@email.com"));
        when(representativesInbox.getChildrenSolicitorEmails(caseData, DIGITAL_SERVICE))
            .thenReturn(newHashSet("sol2@email.com"));
        Set<String> actualRecipients = furtherEvidenceNotificationService.getRepresentativeEmails(caseData);

        assertThat(actualRecipients).containsExactlyInAnyOrder(
            CHILD_1.getSolicitor().getEmail(),
            RESPONDENT_1.getSolicitor().getEmail());
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
    void shouldSendOldNotificationWhenFeatureToggleOff() {
        CaseData caseData = caseData();

        Set<String> recipients = Set.of("test@example.com");

        FurtherEvidenceDocumentUploadedData furtherEvidenceDocumentUploadedData =
            FurtherEvidenceDocumentUploadedData.builder().build();

        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(false);
        when(furtherEvidenceUploadedEmailContentProvider.buildParametersWithHearing(caseData, "Sender",
            DOCUMENTS, Optional.of(caseData.getHearingDetails().get(0).getValue()))).thenReturn(
            furtherEvidenceDocumentUploadedData);

        furtherEvidenceNotificationService.sendNotificationWithHearing(caseData, recipients, "Sender", DOCUMENTS,
            Optional.of(caseData.getHearingDetails().get(0).getValue()));

        verify(notificationService).sendEmail(FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE, recipients,
            furtherEvidenceDocumentUploadedData, CASE_ID.toString());
    }

    @Test
    void shouldSendCorrectNotificationWhenFeatureToggleON() {
        CaseData caseData = caseData();

        Set<String> recipients = Set.of("test@example.com");

        FurtherEvidenceDocumentUploadedData furtherEvidenceDocumentUploadedData =
            FurtherEvidenceDocumentUploadedData.builder().build();

        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        when(furtherEvidenceUploadedEmailContentProvider.buildParametersWithHearing(caseData, "Sender",
            DOCUMENTS, Optional.of(caseData.getHearingDetails().get(0).getValue()))).thenReturn(
            furtherEvidenceDocumentUploadedData);

        furtherEvidenceNotificationService.sendNotificationWithHearing(caseData, recipients, "Sender", DOCUMENTS,
            Optional.of(caseData.getHearingDetails().get(0).getValue()));

        verify(notificationService).sendEmail(DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE, recipients,
            furtherEvidenceDocumentUploadedData, CASE_ID.toString());
    }

    @Test
    void shouldNotSendNotificationWhenNoRecipientsAreProvided() {
        CaseData caseData = caseData();

        Set<String> recipients = Set.of();

        furtherEvidenceNotificationService.sendNotification(caseData, recipients, "Sender", DOCUMENTS);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendNotificationForCourtBundleUploaded() {
        CaseData caseData = caseData();

        Set<String> recipients = Set.of("test@example.com");

        CourtBundleUploadedData courtBundleUploadedData =
            CourtBundleUploadedData.builder().hearingDetails("1st Hearing").build();

        when(courtBundleUploadedEmailContentProvider.buildParameters(caseData, "1st Hearing")).thenReturn(
            courtBundleUploadedData);

        furtherEvidenceNotificationService.sendNotificationForCourtBundleUploaded(caseData, recipients, "1st Hearing");

        verify(notificationService).sendEmail(COURT_BUNDLE_UPLOADED_NOTIFICATION, recipients,
            courtBundleUploadedData, CASE_ID.toString());
    }

    @Test
    void shouldReturnDesignatedLocalAuthorityRecipientsOnly() {
        final CaseData caseData = caseData();

        when(localAuthorityRecipients.getDesignatedLocalAuthorityContacts(any()))
            .thenReturn(new ArrayList<>(LOCAL_AUTHORITY_EMAILS));

        Set<String> actual = furtherEvidenceNotificationService.getDesignatedLocalAuthorityRecipientsOnly(caseData);

        assertThat(actual).isEqualTo(LOCAL_AUTHORITY_EMAILS);
        verify(localAuthorityRecipients).getDesignatedLocalAuthorityContacts(caseData);
    }

    @Test
    void shouldReturnSecondaryLocalAuthorityContactsOnly() {
        final CaseData caseData = caseData();

        when(localAuthorityRecipients.getSecondaryLocalAuthorityContacts(any()))
            .thenReturn(new ArrayList<>(LOCAL_AUTHORITY_EMAILS));

        Set<String> actual = furtherEvidenceNotificationService.getSecondaryLocalAuthorityRecipientsOnly(caseData);

        assertThat(actual).isEqualTo(LOCAL_AUTHORITY_EMAILS);
        verify(localAuthorityRecipients).getSecondaryLocalAuthorityContacts(caseData);
    }

    @Test
    void shouldReturnLegalRepresentativeOnly() {
        final CaseData caseData = caseData();

        when(localAuthorityRecipients.getLegalRepresentatives(any()))
            .thenReturn(new ArrayList<>(LOCAL_AUTHORITY_EMAILS));

        Set<String> actual = furtherEvidenceNotificationService.getLegalRepresentativeOnly(caseData);

        assertThat(actual).isEqualTo(LOCAL_AUTHORITY_EMAILS);
        verify(localAuthorityRecipients).getLegalRepresentatives(caseData);
    }

    @Test
    void shouldReturnFallbackInboxOnly() {
        when(localAuthorityRecipients.getFallbackInbox()).thenReturn(new ArrayList<>(LOCAL_AUTHORITY_EMAILS));

        Set<String> actual = furtherEvidenceNotificationService.getFallbackInbox();

        assertThat(actual).isEqualTo(LOCAL_AUTHORITY_EMAILS);
        verify(localAuthorityRecipients).getFallbackInbox();
    }

    private CaseData caseData() {
        final List<Element<HearingBooking>> hearingBooking = wrapElements(testHearing());
        return CaseData.builder()
            .hearingDetails(hearingBooking)
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY)
            .representatives(wrapElements())
            .build();
    }

    private CaseData caseData(Representative... representatives) {
        return CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY)
            .representatives(wrapElements(representatives))
            .build();
    }

    private CaseData caseData(Child children, Respondent respondents) {
        return CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY)
            .respondents1(wrapElements(respondents))
            .children1(wrapElements(children))
            .build();
    }

    HearingBooking testHearing() {
        return HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .status(ADJOURNED)
            .startDate(LocalDateTime.now())
            .endDate(LocalDateTime.now().plusDays(1))
            .endDateDerived("No")
            .hearingJudgeLabel("Her Honour Judge Judy")
            .legalAdvisorLabel("")
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .build())
            .others(emptyList())
            .venueCustomAddress(Address.builder().build())
            .venue("96")
            .attendance(List.of(IN_PERSON))
            .othersNotified("")
            .build();
    }

    private static List<String> buildDocumentsNamesList() {
        return List.of("DOCUMENT 1", "DOCUMENT 2");
    }
}
