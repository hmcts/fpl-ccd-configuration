package uk.gov.hmcts.reform.fpl.handlers;

import org.apache.commons.lang3.RandomUtils;
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
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.additionalapplicationsuploaded.AdditionalApplicationsUploadedTemplate;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.AdditionalApplicationsUploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdditionalApplicationsUploadedEventHandlerTest {
    private static final DocumentReference TEST_DOCUMENT = mock(DocumentReference.class);
    private static final Long CASE_ID = 12345L;
    private static final String EMAIL_REP_1 = "email-rep1@test.com";
    private static final String EMAIL_REP_2 = "email-rep2@test.com";
    private static final Set<String> EMAIL_REPS = new HashSet<>(Set.of(EMAIL_REP_1, EMAIL_REP_2));
    private static final String DIGITAL_REP_1 = "digital-rep1@test.com";
    private static final String DIGITAL_REP_2 = "digital-rep2@test.com";
    private static final Set<String> DIGITAL_REPS = new HashSet<>(Set.of(DIGITAL_REP_1, DIGITAL_REP_2));
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
        .type(LOCAL_AUTHORITY)
        .name(LOCAL_AUTHORITY_NAME)
        .build();

    @Mock
    private RequestData requestData;
    @Mock
    private NotificationService notificationService;
    @Mock
    private CourtService courtService;
    @Mock
    private AdditionalApplicationsUploadedEmailContentProvider contentProvider;
    @Mock
    private LocalAuthorityRecipientsService localAuthorityRecipients;
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
    @Mock
    private CaseData caseData;
    @Mock
    private AdditionalApplicationsUploadedTemplate notifyData;

    @InjectMocks
    private AdditionalApplicationsUploadedEventHandler underTest;

    @BeforeEach
    void before() {
        given(caseData.getId()).willReturn(CASE_ID);
        given(contentProvider.getNotifyData(caseData)).willReturn(notifyData);
    }

    @Test
    void shouldNotifyDigitalRepresentativesOnAdditionalApplicationsUploadWhenServingOthersIsToggledOn() {
        given(featureToggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(true);

        given(caseData.getAdditionalApplicationsBundle()).willReturn(wrapElements(
            AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .document(TEST_DOCUMENT)
                    .respondents(emptyList())
                    .others(emptyList())
                    .build())
                .build()
        ));

        given(representativesInbox.getEmailsByPreference(caseData, DIGITAL_SERVICE)).willReturn(DIGITAL_REPS);
        given(otherRecipientsInbox.getNonSelectedRecipients(
            eq(DIGITAL_SERVICE), eq(caseData), eq(NO_RECIPIENTS), any()
        )).willReturn(Collections.emptySet());
        given(representativesInbox.getNonSelectedRespondentRecipients(
            eq(DIGITAL_SERVICE), eq(caseData), eq(emptyList()), any()
        )).willReturn(Collections.emptySet());

        underTest.notifyDigitalRepresentatives(
            new AdditionalApplicationsUploadedEvent(caseData, ORDER_APPLICANT_LA)
        );

        verify(representativeNotificationService).sendNotificationToRepresentatives(
            CASE_ID, notifyData, DIGITAL_REPS, INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS
        );
    }

    @Test
    void shouldNotifyEmailRepresentativesOnAdditionalApplicationsUploadWhenServingOthersIsToggledOn() {
        given(featureToggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(true);
        given(caseData.getAdditionalApplicationsBundle()).willReturn(wrapElements(
            AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .document(TEST_DOCUMENT)
                    .respondents(emptyList())
                    .others(emptyList())
                    .build())
                .build()
        ));

        given(representativesInbox.getEmailsByPreference(caseData, EMAIL)).willReturn(EMAIL_REPS);
        given(otherRecipientsInbox.getNonSelectedRecipients(eq(EMAIL), eq(caseData), eq(NO_RECIPIENTS), any()))
            .willReturn(Collections.emptySet());
        given(representativesInbox.getNonSelectedRespondentRecipients(eq(EMAIL), eq(caseData), eq(emptyList()), any()))
            .willReturn(Collections.emptySet());

        underTest.notifyEmailServedRepresentatives(
            new AdditionalApplicationsUploadedEvent(caseData, ORDER_APPLICANT_LA)
        );

        verify(representativeNotificationService).sendNotificationToRepresentatives(
            CASE_ID, notifyData, EMAIL_REPS, INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS
        );
    }

    @Test
    void shouldNotifyLocalAuthorityWhenApplicantIsLocalAuthorityAndServingOthersIsToggledOn() {
        given(featureToggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(true);
        given(caseData.getAdditionalApplicationsBundle()).willReturn(wrapElements(
            AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .document(TEST_DOCUMENT)
                    .applicantName(LOCAL_AUTHORITY_NAME)
                    .others(emptyList())
                    .build())
                .build()
        ));
        given(caseData.getCaseLocalAuthorityName()).willReturn(LOCAL_AUTHORITY_NAME);
        given(localAuthorityRecipients.getRecipients(any())).willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        underTest.notifyApplicant(new AdditionalApplicationsUploadedEvent(caseData, ORDER_APPLICANT_LA));

        final RecipientsRequest expectedRecipientsRequest = RecipientsRequest.builder()
            .caseData(caseData)
            .secondaryLocalAuthorityExcluded(true)
            .build();

        verify(localAuthorityRecipients).getRecipients(expectedRecipientsRequest);
    }

    @Test
    void shouldNotNotifyApplicantWhenApplicantsEmailAddressIsEmptyAndServingOthersIsToggledOn() {
        given(featureToggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(true);

        final String applicantName = "someone";
        given(caseData.getAdditionalApplicationsBundle()).willReturn(wrapElements(
            AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .document(TEST_DOCUMENT)
                    .applicantName(applicantName)
                    .others(emptyList())
                    .build())
                .build()
        ));

        underTest.notifyApplicant(new AdditionalApplicationsUploadedEvent(caseData,
            OrderApplicant.builder().type(OTHER).name(applicantName).build())
        );

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotNotifyRespondentWhenEmailAddressIsEmptyAndServingOthersIsToggledOn() {
        given(featureToggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(true);

        final String applicantName = "John Smith";
        given(caseData.getAdditionalApplicationsBundle()).willReturn(wrapElements(
            AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .document(TEST_DOCUMENT)
                    .applicantName(applicantName)
                    .others(emptyList())
                    .build())
                .build()
        ));

        final Respondent respondent = Respondent.builder()
            .party(RespondentParty.builder().firstName("John").lastName("Smith").build())
            .solicitor(RespondentSolicitor.builder().build())
            .build();

        given(caseData.getRespondents1()).willReturn(wrapElements(respondent));

        underTest.notifyApplicant(new AdditionalApplicationsUploadedEvent(
            caseData, OrderApplicant.builder().type(RESPONDENT).name(applicantName).build())
        );

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotifyRespondentWhenApplicantIsRespondentAndServingOthersIsToggledOn() {
        List<Element<Respondent>> respondents = wrapElements(
            Respondent.builder()
                .party(RespondentParty.builder().firstName("John").lastName("Smith").build())
                .solicitor(RespondentSolicitor.builder().email("respondent1@test.com").build())
                .build(),
            Respondent.builder()
                .party(RespondentParty.builder().firstName("Ross").lastName("Bob").build())
                .solicitor(RespondentSolicitor.builder().build())
                .build()
        );

        given(featureToggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(true);
        given(caseData.getAllRespondents()).willReturn(respondents);
        given(caseData.getAdditionalApplicationsBundle()).willReturn(wrapElements(
            AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .document(TEST_DOCUMENT)
                    .applicantName("John Smith")
                    .others(emptyList())
                    .build())
                .build()
        ));

        OrderApplicant applicant = OrderApplicant.builder().name("John Smith").type(RESPONDENT).build();
        underTest.notifyApplicant(new AdditionalApplicationsUploadedEvent(caseData, applicant));

        verify(notificationService).sendEmail(
            INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS, Set.of("respondent1@test.com"),
            notifyData, CASE_ID.toString()
        );
    }

    @Test
    void shouldNotBuildNotificationTemplateDataForEmailRepsWhenServingOthersIsToggledOnAndEmailRepsAreEmpty() {
        given(featureToggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(true);
        given(caseData.getAdditionalApplicationsBundle()).willReturn(wrapElements(
            AdditionalApplicationsBundle.builder()
                .otherApplicationsBundle(
                    OtherApplicationsBundle.builder().document(TEST_DOCUMENT).others(SELECTED_OTHERS).build())
                .build()
        ));

        given(representativesInbox.getEmailsByPreference(caseData, EMAIL)).willReturn(emptySet());
        given(otherRecipientsInbox.getNonSelectedRecipients(eq(EMAIL), eq(caseData), eq(SELECTED_OTHERS), any()))
            .willReturn(Collections.emptySet());

        underTest.notifyEmailServedRepresentatives(
            new AdditionalApplicationsUploadedEvent(caseData, ORDER_APPLICANT_LA)
        );

        verifyNoMoreInteractions(representativeNotificationService);
    }

    @ParameterizedTest
    @MethodSource("applicationDataParams")
    void shouldSendUploadedAdditionalApplicationsByPost(AdditionalApplicationsBundle additionalApplicationsBundle,
                                                        List<DocumentReference> documents) {
        final Representative representative1 = mock(Representative.class);
        final Representative representative2 = mock(Representative.class);
        final Representative representative3 = mock(Representative.class);
        final RespondentParty otherRespondent = mock(RespondentParty.class);

        given(featureToggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(true);
        given(caseData.getAdditionalApplicationsBundle()).willReturn(wrapElements(additionalApplicationsBundle));
        given(sendDocumentService.getStandardRecipients(caseData))
            .willReturn(List.of(representative1, representative2, representative3));
        given(otherRecipientsInbox.getNonSelectedRecipients(eq(POST), eq(caseData), eq(SELECTED_OTHERS), any()))
            .willReturn(Set.of(representative1));
        given(representativesInbox.getNonSelectedRespondentRecipientsByPost(eq(caseData), eq(SELECTED_RESPONDENTS)))
            .willReturn(Set.of(representative3));
        given(otherRecipientsInbox.getSelectedRecipientsWithNoRepresentation(SELECTED_OTHERS))
            .willReturn(Set.of(otherRespondent));
        given(representativesInbox.getSelectedRecipientsWithNoRepresentation(SELECTED_RESPONDENTS))
            .willReturn(Set.of(representative2));

        underTest.sendAdditionalApplicationsByPost(
            new AdditionalApplicationsUploadedEvent(caseData, ORDER_APPLICANT_LA)
        );

        verify(sendDocumentService).sendDocuments(caseData, documents, List.of(representative2, otherRespondent));
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendApplicationsByPostWhenServingOthersIsToggledOff() {
        given(featureToggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(false);
        underTest.sendAdditionalApplicationsByPost(
            new AdditionalApplicationsUploadedEvent(caseData, ORDER_APPLICANT_LA)
        );
        verifyNoInteractions(sendDocumentService);
    }

    @Test
    void shouldNotBuildNotificationsToLocalAuthorityAndRepresentativesWhenServingOthersIsToggledOff() {
        given(featureToggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(false);

        underTest.notifyApplicant(new AdditionalApplicationsUploadedEvent(
            caseData, OrderApplicant.builder().type(LOCAL_AUTHORITY).name(LOCAL_AUTHORITY_NAME).build())
        );
        underTest.notifyEmailServedRepresentatives(new AdditionalApplicationsUploadedEvent(caseData, null));
        underTest.notifyDigitalRepresentatives(new AdditionalApplicationsUploadedEvent(caseData, null));

        verifyNoInteractions(notificationService);
        verifyNoInteractions(representativeNotificationService);
    }

    @Test
    void shouldNotifyNonHmctsAdminOnAdditionalApplicationsUpload() {
        CaseData caseData = caseData();

        given(requestData.userRoles()).willReturn(Set.of("caseworker-publiclaw-solicitor"));
        given(courtService.getCourtEmail(caseData)).willReturn("hmcts-non-admin@test.com");
        given(contentProvider.getNotifyData(caseData)).willReturn(notifyData);

        underTest.notifyAdmin(new AdditionalApplicationsUploadedEvent(caseData, ORDER_APPLICANT_LA));

        verify(notificationService).sendEmail(
            INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_CTSC, "hmcts-non-admin@test.com", notifyData, caseData.getId()
        );
    }

    @Test
    void shouldNotifyCtscAdminOnAdditionalApplicationsUploadWhenCtscIsEnabled() {
        CaseData caseData = CaseData.builder()
            .id(RandomUtils.nextLong())
            .sendToCtsc("Yes")
            .build();

        given(requestData.userRoles()).willReturn(Set.of("caseworker-publiclaw-solicitor"));
        given(courtService.getCourtEmail(caseData)).willReturn("Ctsc+test@gmail.com");

        given(localAuthorityRecipients.getRecipients(
            RecipientsRequest.builder().caseData(caseData).build()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(contentProvider.getNotifyData(caseData)).willReturn(notifyData);

        underTest.notifyAdmin(new AdditionalApplicationsUploadedEvent(caseData, ORDER_APPLICANT_LA));

        verify(notificationService).sendEmail(
            INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_CTSC, CTSC_INBOX, notifyData, caseData.getId()
        );
    }

    @Test
    void shouldNotNotifyHmctsAdminOnAdditionalApplicationsUpload() {
        given(requestData.userRoles()).willReturn(
            new HashSet<>(Set.of("caseworker", "caseworker-publiclaw", "caseworker-publiclaw-courtadmin"))
        );

        underTest.notifyAdmin(new AdditionalApplicationsUploadedEvent(caseData, ORDER_APPLICANT_LA));

        verifyNoInteractions(notificationService);
    }

    private static Stream<Arguments> applicationDataParams() {
        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .document(C2_DOCUMENT)
            .supplementsBundle(wrapElements(Supplement.builder().document(SUPPLEMENT_1).build()))
            .supportingEvidenceBundle(wrapElements(
                SupportingEvidenceBundle.builder().document(SUPPORTING_DOCUMENT_1).build()
            ))
            .others(SELECTED_OTHERS)
            .respondents(SELECTED_RESPONDENTS)
            .build();

        OtherApplicationsBundle otherApplicationsBundle = OtherApplicationsBundle.builder()
            .document(OTHER_APPLICATION_DOCUMENT)
            .supplementsBundle(wrapElements(Supplement.builder().document(SUPPLEMENT_2).build()))
            .supportingEvidenceBundle(wrapElements(
                SupportingEvidenceBundle.builder().document(SUPPORTING_DOCUMENT_2).build()
            ))
            .others(SELECTED_OTHERS)
            .respondents(SELECTED_RESPONDENTS)
            .build();

        return Stream.of(
            Arguments.of(
                AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(c2DocumentBundle)
                    .otherApplicationsBundle(otherApplicationsBundle)
                    .build(),
                List.of(
                    C2_DOCUMENT, SUPPLEMENT_1, SUPPORTING_DOCUMENT_1, OTHER_APPLICATION_DOCUMENT, SUPPLEMENT_2,
                    SUPPORTING_DOCUMENT_2
                )
            ),
            Arguments.of(
                AdditionalApplicationsBundle.builder().c2DocumentBundle(c2DocumentBundle).build(),
                List.of(C2_DOCUMENT, SUPPLEMENT_1, SUPPORTING_DOCUMENT_1)
            ),
            Arguments.of(
                AdditionalApplicationsBundle.builder().otherApplicationsBundle(otherApplicationsBundle).build(),
                List.of(OTHER_APPLICATION_DOCUMENT, SUPPLEMENT_2, SUPPORTING_DOCUMENT_2)
            )
        );
    }
}
