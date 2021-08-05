package uk.gov.hmcts.reform.fpl.handlers;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.fpl.events.AdditionalApplicationsUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OrderApplicant;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.additionalapplicationsuploaded.AdditionalApplicationsUploadedTemplate;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.AdditionalApplicationsUploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.RESPONDENT;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_NAME;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdditionalApplicationsUploadedEventHandlerTest {
    @Mock
    private RequestData requestData;
    @Mock
    private NotificationService notificationService;
    @Mock
    private CourtService courtService;
    @Mock
    private AdditionalApplicationsUploadedEmailContentProvider additionalApplicationsUploadedEmailContentProvider;
    @Mock
    private InboxLookupService inboxLookupService;
    @Mock
    private RepresentativesInbox representativesInbox;
    @Mock
    private OtherRecipientsInbox otherRecipientsInbox;
    @Mock
    private RepresentativeNotificationService representativeNotificationService;
    @Mock
    private SendDocumentService sendDocumentService;
    @Mock
    private FeatureToggleService featureToggleService;
    @InjectMocks
    private AdditionalApplicationsUploadedEventHandler underTest;

    private static final CaseData CASE_DATA = mock(CaseData.class);
    private static final DocumentReference TEST_DOCUMENT = mock(DocumentReference.class);
    private static final Long CASE_ID = 12345L;

    private static final String EMAIL_REP_1 = "email-rep1@test.com";
    private static final String EMAIL_REP_2 = "email-rep2@test.com";
    private static final Set<String> EMAIL_REPS = new HashSet<>(Arrays.asList(EMAIL_REP_1, EMAIL_REP_2));
    private static final String DIGITAL_REP_1 = "digital-rep1@test.com";
    private static final String DIGITAL_REP_2 = "digital-rep2@test.com";
    private static final Set<String> DIGITAL_REPS = new HashSet<>(Arrays.asList(DIGITAL_REP_1, DIGITAL_REP_2));
    private static final List<Element<Other>> NO_RECIPIENTS = Collections.emptyList();
    private static final List<Element<Other>> SELECTED_OTHERS = List.of(element(mock(Other.class)));
    private static final List<Element<Respondent>> SELECTED_RESPONDENTS = List.of(element(mock(Respondent.class)));
    private static final DocumentReference C2_DOCUMENT = testDocumentReference();
    private static final DocumentReference OTHER_APPLICATION_DOCUMENT = testDocumentReference();
    private static final DocumentReference SUPPLEMENT_1 = testDocumentReference();
    private static final DocumentReference SUPPLEMENT_2 = testDocumentReference();
    private static final DocumentReference SUPPORTING_DOCUMENT_1 = testDocumentReference();
    private static final DocumentReference SUPPORTING_DOCUMENT_2 = testDocumentReference();
    private static final OrderApplicant ORDER_APPLICANT_LA = OrderApplicant.builder()
        .type(LOCAL_AUTHORITY).name(LOCAL_AUTHORITY_NAME).build();

    final String subjectLine = "Lastname, SACCCCCCCC5676576567";
    AdditionalApplicationsUploadedTemplate additionalApplicationsParameters =
        getAdditionalApplicationsUploadedTemplateParameters();

    @BeforeEach
    void before() {
        given(CASE_DATA.getId()).willReturn(CASE_ID);
        given(additionalApplicationsUploadedEmailContentProvider.getNotifyData(CASE_DATA))
            .willReturn(additionalApplicationsParameters);
    }

    @Test
    void shouldNotifyDigitalRepresentativesOnAdditionalApplicationsUploadWhenServingOthersIsToggledOn() {
        given(featureToggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(true);

        given(CASE_DATA.getAdditionalApplicationsBundle())
            .willReturn(wrapElements(AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder().document(TEST_DOCUMENT)
                    .respondents(emptyList()).others(emptyList()).build())
                .build()));

        given(representativesInbox.getEmailsByPreference(CASE_DATA, DIGITAL_SERVICE)).willReturn(DIGITAL_REPS);
        given(otherRecipientsInbox.getNonSelectedRecipients(
            eq(DIGITAL_SERVICE), eq(CASE_DATA), eq(NO_RECIPIENTS), any()))
            .willReturn(Collections.emptySet());
        given(representativesInbox.getNonSelectedRespondentsRecipients(
            eq(DIGITAL_SERVICE), eq(CASE_DATA), eq(emptyList()), any()))
            .willReturn(Collections.emptySet());

        underTest.notifyDigitalRepresentatives(
            new AdditionalApplicationsUploadedEvent(CASE_DATA, ORDER_APPLICANT_LA));

        verify(representativeNotificationService).sendNotificationToRepresentatives(
            CASE_ID,
            additionalApplicationsParameters,
            DIGITAL_REPS,
            INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS);
    }

    @Test
    void shouldNotifyEmailRepresentativesOnAdditionalApplicationsUploadWhenServingOthersIsToggledOn() {
        given(featureToggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(true);
        given(CASE_DATA.getAdditionalApplicationsBundle())
            .willReturn(wrapElements(AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder().document(TEST_DOCUMENT)
                    .respondents(emptyList())
                    .others(emptyList()).build())
                .build()));
        given(representativesInbox.getEmailsByPreference(CASE_DATA, EMAIL)).willReturn(EMAIL_REPS);
        given(otherRecipientsInbox.getNonSelectedRecipients(eq(EMAIL), eq(CASE_DATA), eq(NO_RECIPIENTS), any()))
            .willReturn(Collections.emptySet());
        given(representativesInbox.getNonSelectedRespondentsRecipients(
            eq(EMAIL), eq(CASE_DATA), eq(emptyList()), any()))
            .willReturn(Collections.emptySet());

        underTest.notifyEmailServedRepresentatives(
            new AdditionalApplicationsUploadedEvent(CASE_DATA, ORDER_APPLICANT_LA));

        verify(representativeNotificationService).sendNotificationToRepresentatives(
            CASE_ID,
            additionalApplicationsParameters,
            EMAIL_REPS,
            INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS);
    }

    @Test
    void shouldNotifyLocalAuthorityWhenApplicantIsLocalAuthorityAndServingOthersIsToggledOn() {
        given(featureToggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(true);
        given(CASE_DATA.getAdditionalApplicationsBundle())
            .willReturn(wrapElements(AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder().document(TEST_DOCUMENT)
                    .applicantName(LOCAL_AUTHORITY_NAME).others(emptyList()).build())
                .build()));
        given(CASE_DATA.getCaseLocalAuthorityName()).willReturn(LOCAL_AUTHORITY_NAME);
        given(inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(CASE_DATA).build()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        underTest.notifyApplicant(new AdditionalApplicationsUploadedEvent(CASE_DATA, ORDER_APPLICANT_LA));

        verify(notificationService).sendEmail(
            INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            additionalApplicationsParameters,
            CASE_ID.toString());
    }

    @Test
    void shouldNotNotifyApplicantWhenApplicantsEmailAddressIsEmptyAndServingOthersIsToggledOn() {
        given(featureToggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(true);

        final String applicantName = "someone";
        given(CASE_DATA.getAdditionalApplicationsBundle())
            .willReturn(wrapElements(AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder().document(TEST_DOCUMENT)
                    .applicantName(applicantName).others(emptyList()).build())
                .build()));

        underTest.notifyApplicant(new AdditionalApplicationsUploadedEvent(CASE_DATA,
            OrderApplicant.builder().type(OTHER).name(applicantName).build()));

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotNotifyRespondentWhenEmailAddressIsEmptyAndServingOthersIsToggledOn() {
        given(featureToggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(true);

        final String applicantName = "John Smith";
        given(CASE_DATA.getAdditionalApplicationsBundle())
            .willReturn(wrapElements(AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder().document(TEST_DOCUMENT)
                    .applicantName(applicantName).others(emptyList()).build())
                .build()));

        final Respondent respondent = Respondent.builder()
            .party(RespondentParty.builder().firstName("John").lastName("Smith").build())
            .solicitor(RespondentSolicitor.builder().build()).build();
        given(CASE_DATA.getRespondents1()).willReturn(wrapElements(respondent));

        underTest.notifyApplicant(new AdditionalApplicationsUploadedEvent(
            CASE_DATA, OrderApplicant.builder().type(RESPONDENT).name(applicantName).build()));

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotifyRespondentWhenApplicantIsRespondentAndServingOthersIsToggledOn() {
        List<Element<Respondent>> respondents = wrapElements(Respondent.builder()
                .party(RespondentParty.builder().firstName("John").lastName("Smith").build())
                .solicitor(RespondentSolicitor.builder().email("respondent1@test.com").build()).build(),
            Respondent.builder()
                .party(RespondentParty.builder().firstName("Ross").lastName("Bob").build())
                .solicitor(RespondentSolicitor.builder().build()).build());

        given(featureToggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(true);
        given(CASE_DATA.getAllRespondents()).willReturn(respondents);
        given(CASE_DATA.getAdditionalApplicationsBundle())
            .willReturn(wrapElements(AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder().document(TEST_DOCUMENT)
                    .applicantName("John Smith").others(emptyList()).build())
                .build()));

        OrderApplicant applicant = OrderApplicant.builder().name("John Smith").type(RESPONDENT).build();
        underTest.notifyApplicant(new AdditionalApplicationsUploadedEvent(CASE_DATA, applicant));

        verify(notificationService).sendEmail(
            INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS,
            List.of("respondent1@test.com"),
            additionalApplicationsParameters,
            CASE_ID.toString());
    }

    @Test
    void shouldNotBuildNotificationTemplateDataForEmailRepsWhenServingOthersIsToggledOnAndEmailRepsAreEmpty() {
        given(featureToggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(true);
        given(CASE_DATA.getAdditionalApplicationsBundle())
            .willReturn(wrapElements(AdditionalApplicationsBundle.builder()
                .otherApplicationsBundle(
                    OtherApplicationsBundle.builder().document(TEST_DOCUMENT).others(SELECTED_OTHERS).build())
                .build()));

        given(representativesInbox.getEmailsByPreference(CASE_DATA, EMAIL)).willReturn(emptySet());
        given(otherRecipientsInbox.getNonSelectedRecipients(eq(EMAIL), eq(CASE_DATA), eq(SELECTED_OTHERS), any()))
            .willReturn(Collections.emptySet());

        underTest.notifyEmailServedRepresentatives(
            new AdditionalApplicationsUploadedEvent(CASE_DATA, ORDER_APPLICANT_LA));

        verifyNoMoreInteractions(representativeNotificationService);
    }

    @ParameterizedTest
    @MethodSource("applicationDataParams")
    @SuppressWarnings("unchecked")
    void shouldSendUploadedAdditionalApplicationsByPost(
        AdditionalApplicationsBundle additionalApplicationsBundle,
        List<DocumentReference> documents) {
        final Representative representative1 = mock(Representative.class);
        final Representative representative2 = mock(Representative.class);
        final Representative representative3 = mock(Representative.class);
        final RespondentParty otherRespondent = mock(RespondentParty.class);

        given(featureToggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(true);
        given(CASE_DATA.getAdditionalApplicationsBundle()).willReturn(wrapElements(additionalApplicationsBundle));
        given(sendDocumentService.getStandardRecipients(CASE_DATA))
            .willReturn(List.of(representative1, representative2, representative3));
        given(otherRecipientsInbox.getNonSelectedRecipients(eq(POST), eq(CASE_DATA), eq(SELECTED_OTHERS), any()))
            .willReturn((Set) Set.of(representative1));
        given(representativesInbox.getNonSelectedRespondentsRecipients(
            eq(POST), eq(CASE_DATA), eq(SELECTED_RESPONDENTS), any()))
            .willReturn((Set) Set.of(representative3));
        given(otherRecipientsInbox.getSelectedRecipientsWithNoRepresentation(SELECTED_OTHERS))
            .willReturn(Set.of(otherRespondent));
        given(representativesInbox.getSelectedRecipientsWithNoRepresentation(SELECTED_RESPONDENTS))
            .willReturn(Set.of(representative2));

        underTest.sendAdditionalApplicationsByPost(
            new AdditionalApplicationsUploadedEvent(CASE_DATA, ORDER_APPLICANT_LA));

        verify(sendDocumentService).sendDocuments(CASE_DATA, documents, List.of(representative2, otherRespondent));
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendApplicationsByPostWhenServingOthersIsToggledOff() {
        given(featureToggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(false);
        underTest.sendAdditionalApplicationsByPost(
            new AdditionalApplicationsUploadedEvent(CASE_DATA, ORDER_APPLICANT_LA));
        verifyNoInteractions(sendDocumentService);
    }

    @Test
    void shouldNotBuildNotificationsToLocalAuthorityAndRepresentativesWhenServingOthersIsToggledOff() {
        given(featureToggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(false);

        underTest.notifyApplicant(new AdditionalApplicationsUploadedEvent(
            CASE_DATA, OrderApplicant.builder().type(LOCAL_AUTHORITY).name(LOCAL_AUTHORITY_NAME).build()));
        underTest.notifyEmailServedRepresentatives(new AdditionalApplicationsUploadedEvent(CASE_DATA, null));
        underTest.notifyDigitalRepresentatives(new AdditionalApplicationsUploadedEvent(CASE_DATA, null));

        verifyNoInteractions(notificationService);
        verifyNoInteractions(representativeNotificationService);
    }

    private static Stream<Arguments> applicationDataParams() {
        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .document(C2_DOCUMENT)
            .supplementsBundle(wrapElements(Supplement.builder().document(SUPPLEMENT_1).build()))
            .supportingEvidenceBundle(
                wrapElements(SupportingEvidenceBundle.builder().document(SUPPORTING_DOCUMENT_1).build()))
            .others(SELECTED_OTHERS)
            .respondents(SELECTED_RESPONDENTS).build();

        OtherApplicationsBundle otherApplicationsBundle = OtherApplicationsBundle.builder()
            .document(OTHER_APPLICATION_DOCUMENT)
            .supplementsBundle(wrapElements(Supplement.builder().document(SUPPLEMENT_2).build()))
            .supportingEvidenceBundle(
                wrapElements(SupportingEvidenceBundle.builder().document(SUPPORTING_DOCUMENT_2).build()))
            .others(SELECTED_OTHERS)
            .respondents(SELECTED_RESPONDENTS).build();

        return Stream.of(
            Arguments.of(AdditionalApplicationsBundle.builder().c2DocumentBundle(c2DocumentBundle)
                    .otherApplicationsBundle(otherApplicationsBundle).build(),
                List.of(C2_DOCUMENT, SUPPLEMENT_1, SUPPORTING_DOCUMENT_1,
                    OTHER_APPLICATION_DOCUMENT, SUPPLEMENT_2, SUPPORTING_DOCUMENT_2)),
            Arguments.of(AdditionalApplicationsBundle.builder().c2DocumentBundle(c2DocumentBundle).build(),
                List.of(C2_DOCUMENT, SUPPLEMENT_1, SUPPORTING_DOCUMENT_1)),
            Arguments.of(AdditionalApplicationsBundle.builder()
                    .otherApplicationsBundle(otherApplicationsBundle).build(),
                List.of(OTHER_APPLICATION_DOCUMENT, SUPPLEMENT_2, SUPPORTING_DOCUMENT_2)));
    }

    @Test
    void shouldNotifyNonHmctsAdminOnAdditionalApplicationsUpload() {
        CaseData caseData = caseData();

        given(requestData.userRoles()).willReturn(Set.of("caseworker-publiclaw-solicitor"));
        given(courtService.getCourtEmail(caseData))
            .willReturn("hmcts-non-admin@test.com");
        given(additionalApplicationsUploadedEmailContentProvider.getNotifyData(caseData))
            .willReturn(additionalApplicationsParameters);

        underTest.notifyAdmin(new AdditionalApplicationsUploadedEvent(caseData, ORDER_APPLICANT_LA));

        verify(notificationService).sendEmail(
            INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_CTSC,
            "hmcts-non-admin@test.com",
            additionalApplicationsParameters,
            caseData.getId());
    }

    @Test
    void shouldNotifyCtscAdminOnAdditionalApplicationsUploadWhenCtscIsEnabled() {
        CaseData caseData = CaseData.builder()
            .id(RandomUtils.nextLong())
            .sendToCtsc("Yes")
            .build();

        given(requestData.userRoles()).willReturn(Set.of("caseworker-publiclaw-solicitor"));
        given(courtService.getCourtEmail(caseData)).willReturn("Ctsc+test@gmail.com");

        given(inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(additionalApplicationsUploadedEmailContentProvider.getNotifyData(caseData))
            .willReturn(additionalApplicationsParameters);

        underTest.notifyAdmin(new AdditionalApplicationsUploadedEvent(caseData, ORDER_APPLICANT_LA));

        verify(notificationService).sendEmail(
            INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_CTSC,
            CTSC_INBOX,
            additionalApplicationsParameters,
            caseData.getId());
    }

    @Test
    void shouldNotNotifyHmctsAdminOnAdditionalApplicationsUpload() {
        given(requestData.userRoles()).willReturn(new HashSet<>(Arrays.asList("caseworker", "caseworker-publiclaw",
            "caseworker-publiclaw-courtadmin")));

        underTest.notifyAdmin(new AdditionalApplicationsUploadedEvent(CASE_DATA, ORDER_APPLICANT_LA));

        verifyNoInteractions(notificationService);
    }

    private AdditionalApplicationsUploadedTemplate getAdditionalApplicationsUploadedTemplateParameters() {
        String fileContent = new String(Base64.encodeBase64(DOCUMENT_CONTENT), ISO_8859_1);
        JSONObject jsonFileObject = new JSONObject().put("file", fileContent);

        return AdditionalApplicationsUploadedTemplate.builder()
            .callout(subjectLine)
            .lastName("Smith")
            .childLastName("Jones")
            .caseUrl("null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345#C2Tab")
            .documentLink(jsonFileObject.toMap())
            .applicationTypes(Arrays.asList("C2", "C13A - Special guardianship order"))
            .build();
    }
}
