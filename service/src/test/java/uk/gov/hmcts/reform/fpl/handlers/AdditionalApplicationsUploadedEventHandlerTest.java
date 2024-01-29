package uk.gov.hmcts.reform.fpl.handlers;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.ApplicantType;
import uk.gov.hmcts.reform.fpl.events.AdditionalApplicationsUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.OrderApplicant;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.cafcass.NewDocumentData;
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
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.AdditionalApplicationsUploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.CHILD;
import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.RESPONDENT;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_NAME;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.SECONDARY_LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.ADDITIONAL_DOCUMENT;
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
    private static final List<Element<Respondent>> RESPONDENTS = List.of(element(mock(Respondent.class)));
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
    private CaseData caseData;
    @Mock
    private CaseData caseDataBefore;
    @Mock
    private AdditionalApplicationsUploadedTemplate notifyData;
    @Mock
    private CafcassNotificationService cafcassNotificationService;
    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;
    @Mock
    private FeatureToggleService featureToggleService;
    @Captor
    private ArgumentCaptor<NewDocumentData> newDocumentDataArgumentCaptor;

    @InjectMocks
    private AdditionalApplicationsUploadedEventHandler underTest;

    @BeforeEach
    void before() {
        given(caseData.getId()).willReturn(CASE_ID);
        given(contentProvider.getNotifyData(caseData)).willReturn(notifyData);
        given(featureToggleService.isWATaskEmailsEnabled()).willReturn(true);
    }

    @Test
    void shouldNotifyDigitalRepresentativesOnAdditionalApplicationsUpload() {
        given(caseData.getAdditionalApplicationsBundle()).willReturn(wrapElements(
            AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .document(TEST_DOCUMENT)
                    .respondents(emptyList())
                    .build())
                .build()
        ));

        given(representativesInbox.getEmailsByPreference(caseData, DIGITAL_SERVICE)).willReturn(DIGITAL_REPS);

        underTest.notifyDigitalRepresentatives(
            new AdditionalApplicationsUploadedEvent(caseData, caseDataBefore, ORDER_APPLICANT_LA)
        );

        verify(representativeNotificationService).sendNotificationToRepresentatives(
            CASE_ID, notifyData, DIGITAL_REPS, INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS
        );
    }

    @Test
    void shouldNotifyEmailRepresentativesOnAdditionalApplicationsUpload() {
        given(caseData.getAdditionalApplicationsBundle()).willReturn(wrapElements(
            AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .document(TEST_DOCUMENT)
                    .respondents(emptyList())
                    .build())
                .build()
        ));

        given(representativesInbox.getEmailsByPreference(caseData, EMAIL)).willReturn(EMAIL_REPS);

        underTest.notifyEmailServedRepresentatives(
            new AdditionalApplicationsUploadedEvent(caseData, caseDataBefore, ORDER_APPLICANT_LA)
        );

        verify(representativeNotificationService).sendNotificationToRepresentatives(
            CASE_ID, notifyData, EMAIL_REPS, INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS
        );
    }

    @Test
    void shouldNotifyAllLAsNoMatterWhoIsTheApplicant() {
        given(caseData.getAdditionalApplicationsBundle()).willReturn(wrapElements(
            AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .document(TEST_DOCUMENT)
                    .build())
                .build()
        ));
        given(localAuthorityRecipients.getRecipients(
            RecipientsRequest.builder().caseData(caseData).build()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS, SECONDARY_LOCAL_AUTHORITY_EMAIL_ADDRESS));

        Set<OrderApplicant> allApplicants = new HashSet<>();
        for (ApplicantType at : ApplicantType.values()) {
            allApplicants.add(OrderApplicant.builder()
                .type(at)
                .name(at.name())
                .build());
        }

        for (OrderApplicant applicant : allApplicants) {
            underTest.notifyApplicant(
                new AdditionalApplicationsUploadedEvent(caseData, caseDataBefore, applicant)
            );
        }

        verify(notificationService, times(allApplicants.size())).sendEmail(
            INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS, Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS,
                SECONDARY_LOCAL_AUTHORITY_EMAIL_ADDRESS),
            notifyData, CASE_ID.toString()
        );
    }

    @Test
    void shouldNotifyAllLAsAndRespondentWhenApplicantIsRespondent() {
        final String respondent1FirstName = "John";
        final String respondent1LastName = "Smith";
        final String respondent1FullName = respondent1FirstName + " " + respondent1LastName;
        final String respondent1EmailAddress = "respondent1@test.com";
        List<Element<Respondent>> respondents = wrapElements(
            Respondent.builder()
                .party(RespondentParty.builder().firstName(respondent1FirstName).lastName(respondent1LastName)
                    .build())
                .solicitor(RespondentSolicitor.builder().email(respondent1EmailAddress).build())
                .build(),
            Respondent.builder()
                .party(RespondentParty.builder().firstName("Ross").lastName("Bob").build())
                .solicitor(RespondentSolicitor.builder().build())
                .build()
        );

        given(localAuthorityRecipients.getRecipients(
            RecipientsRequest.builder().caseData(caseData).build()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS, SECONDARY_LOCAL_AUTHORITY_EMAIL_ADDRESS));
        given(caseData.getAllRespondents()).willReturn(respondents);
        given(caseData.getAdditionalApplicationsBundle()).willReturn(wrapElements(
            AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .document(TEST_DOCUMENT)
                    .applicantName(respondent1FullName)
                    .build())
                .build()
        ));

        OrderApplicant applicant = OrderApplicant.builder().name(respondent1FullName).type(RESPONDENT).build();
        underTest.notifyApplicant(new AdditionalApplicationsUploadedEvent(caseData, caseDataBefore, applicant));

        verify(notificationService).sendEmail(
            INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS, Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS,
                SECONDARY_LOCAL_AUTHORITY_EMAIL_ADDRESS, respondent1EmailAddress),
            notifyData, CASE_ID.toString()
        );
    }

    @Test
    void shouldNotifyAllLAsAndChildWhenApplicantIsChild() {
        final String child1FirstName = "Jack";
        final String child1LastName = "Smith";
        final String child1FullName = child1FirstName + " " + child1LastName;
        final String child1EmailAddress = "child1@test.com";
        List<Element<Child>> children = wrapElements(
            Child.builder()
                .party(ChildParty.builder().firstName(child1FirstName).lastName(child1LastName)
                    .build())
                .solicitor(RespondentSolicitor.builder().email(child1EmailAddress).build())
                .build(),
            Child.builder()
                .party(ChildParty.builder().firstName("Ross").lastName("Bob").build())
                .solicitor(RespondentSolicitor.builder().build())
                .build()
        );

        given(localAuthorityRecipients.getRecipients(
            RecipientsRequest.builder().caseData(caseData).build()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS, SECONDARY_LOCAL_AUTHORITY_EMAIL_ADDRESS));
        given(caseData.getAllChildren()).willReturn(children);
        given(caseData.getAdditionalApplicationsBundle()).willReturn(wrapElements(
            AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .document(TEST_DOCUMENT)
                    .applicantName(child1FullName)
                    .build())
                .build()
        ));

        OrderApplicant applicant = OrderApplicant.builder().name(child1FullName).type(CHILD).build();
        underTest.notifyApplicant(new AdditionalApplicationsUploadedEvent(caseData, caseDataBefore, applicant));

        verify(notificationService).sendEmail(
            INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS, Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS,
                SECONDARY_LOCAL_AUTHORITY_EMAIL_ADDRESS, child1EmailAddress),
            notifyData, CASE_ID.toString()
        );
    }

    @Test
    void shouldNotNotifyApplicantWhenApplicantsEmailAddressIsEmpty() {
        final String applicantName = "someone";
        given(caseData.getAdditionalApplicationsBundle()).willReturn(wrapElements(
            AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .document(TEST_DOCUMENT)
                    .applicantName(applicantName)
                    .build())
                .build()
        ));

        underTest.notifyApplicant(new AdditionalApplicationsUploadedEvent(caseData, caseDataBefore,
                OrderApplicant.builder().type(OTHER).name(applicantName).build())
        );

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotNotifyRespondentWhenEmailAddressIsEmpty() {
        final String applicantName = "John Smith";
        given(caseData.getAdditionalApplicationsBundle()).willReturn(wrapElements(
            AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .document(TEST_DOCUMENT)
                    .applicantName(applicantName)
                    .build())
                .build()
        ));

        final Respondent respondent = Respondent.builder()
            .party(RespondentParty.builder().firstName("John").lastName("Smith").build())
            .solicitor(RespondentSolicitor.builder().build())
            .build();

        given(caseData.getRespondents1()).willReturn(wrapElements(respondent));

        underTest.notifyApplicant(new AdditionalApplicationsUploadedEvent(
            caseData, caseDataBefore, OrderApplicant.builder().type(RESPONDENT).name(applicantName).build())
        );

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotifyRespondentWhenApplicantIsRespondent() {
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

        given(caseData.getAllRespondents()).willReturn(respondents);
        given(caseData.getAdditionalApplicationsBundle()).willReturn(wrapElements(
            AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .document(TEST_DOCUMENT)
                    .applicantName("John Smith")
                    .build())
                .build()
        ));

        OrderApplicant applicant = OrderApplicant.builder().name("John Smith").type(RESPONDENT).build();
        underTest.notifyApplicant(new AdditionalApplicationsUploadedEvent(caseData, caseDataBefore, applicant));

        verify(notificationService).sendEmail(
            INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS, Set.of("respondent1@test.com"),
            notifyData, CASE_ID.toString()
        );
    }

    @Test
    void shouldNotBuildNotificationTemplateDataForEmailRepsWhenEmailRepsAreEmpty() {
        given(caseData.getAdditionalApplicationsBundle()).willReturn(wrapElements(
            AdditionalApplicationsBundle.builder()
                .otherApplicationsBundle(
                    OtherApplicationsBundle.builder().document(TEST_DOCUMENT).build())
                .build()
        ));

        given(representativesInbox.getEmailsByPreference(caseData, EMAIL)).willReturn(emptySet());

        underTest.notifyEmailServedRepresentatives(
            new AdditionalApplicationsUploadedEvent(caseData, caseDataBefore, ORDER_APPLICANT_LA)
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

        given(caseData.getAdditionalApplicationsBundle()).willReturn(wrapElements(additionalApplicationsBundle));
        given(sendDocumentService.getStandardRecipients(caseData))
            .willReturn(List.of(representative1, representative2, representative3));

        underTest.sendAdditionalApplicationsByPost(
            new AdditionalApplicationsUploadedEvent(caseData, caseDataBefore, ORDER_APPLICANT_LA)
        );

        verify(sendDocumentService).sendDocuments(caseData, documents,
            List.of(representative1, representative2, representative3));
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotifyNonHmctsAdminOnAdditionalApplicationsUpload() {
        CaseData caseData = caseData();

        given(requestData.userRoles()).willReturn(Set.of("caseworker-publiclaw-solicitor"));
        given(courtService.getCourtEmail(caseData)).willReturn("hmcts-non-admin@test.com");
        given(contentProvider.getNotifyData(caseData)).willReturn(notifyData);

        underTest.notifyAdmin(new AdditionalApplicationsUploadedEvent(caseData, caseDataBefore, ORDER_APPLICANT_LA));

        verify(notificationService).sendEmail(
            INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_CTSC,
            "hmcts-non-admin@test.com",
            notifyData,
            caseData.getId()
        );
    }

    @Test
    void shouldNotifyCtscAdminOnAdditionalApplicationsUploadWhenCtscIsEnabledAndWaEmailToggleOn() {
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

        underTest.notifyAdmin(new AdditionalApplicationsUploadedEvent(caseData, caseDataBefore, ORDER_APPLICANT_LA));

        verify(notificationService).sendEmail(
            INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_CTSC, CTSC_INBOX, notifyData, caseData.getId()
        );
    }

    @Test
    void shouldNotNotifyCtscAdminOnAdditionalApplicationsUploadWhenWaEmailToggleOff() {
        CaseData caseData = CaseData.builder()
            .id(RandomUtils.nextLong())
            .sendToCtsc("Yes")
            .build();

        given(featureToggleService.isWATaskEmailsEnabled()).willReturn(false);

        underTest.notifyAdmin(new AdditionalApplicationsUploadedEvent(caseData, caseDataBefore, ORDER_APPLICANT_LA));

        verify(notificationService, never()).sendEmail(
            INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_CTSC, CTSC_INBOX, notifyData, caseData.getId()
        );
    }


    @Test
    void shouldNotNotifyHmctsAdminOnAdditionalApplicationsUpload() {
        given(requestData.userRoles()).willReturn(
            new HashSet<>(Set.of("caseworker", "caseworker-publiclaw", "caseworker-publiclaw-courtadmin"))
        );

        underTest.notifyAdmin(new AdditionalApplicationsUploadedEvent(caseData, caseDataBefore, ORDER_APPLICANT_LA));

        verifyNoInteractions(notificationService);
    }

    @ParameterizedTest
    @MethodSource("applicationDataParams")
    void shouldNotifyCafcassWhenAdditionalDocumentsUploaded(AdditionalApplicationsBundle additionalApplicationsBundle,
                                                        List<DocumentReference> documents) {
        CaseData caseData = CaseData.builder()
                .id(RandomUtils.nextLong())
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .sendToCtsc("Yes")
                .additionalApplicationsBundle(wrapElements(additionalApplicationsBundle))
                .build();

        verifyInvocation(documents, caseData, caseDataBefore);
    }

    @ParameterizedTest
    @MethodSource("applicationDataParams")
    void shouldNotifyCafcassWhenFirstAdditionalDocumentsUploaded(
            AdditionalApplicationsBundle additionalApplicationsBundle,
            List<DocumentReference> documents) {
        CaseData caseData = CaseData.builder()
                .id(RandomUtils.nextLong())
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .sendToCtsc("Yes")
                .additionalApplicationsBundle(wrapElements(additionalApplicationsBundle))
                .build();

        CaseData caseDataBefore = CaseData.builder()
                .id(RandomUtils.nextLong())
                .build();

        verifyInvocation(documents, caseData, caseDataBefore);
    }

    private void verifyInvocation(List<DocumentReference> documents, CaseData caseData, CaseData caseDataBefore) {
        given(cafcassLookupConfiguration.getCafcassEngland(any()))
                .willReturn(
                        Optional.of(
                                new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                        )
            );

        given(contentProvider.getApplicationTypes(caseData.getAdditionalApplicationsBundle().get(0).getValue()))
                .willReturn(Arrays.asList("C2 (With notice) - Appointment of a guardian",
                        "C13A - Special guardianship order",
                        "C20 - Secure accommodation (England)",
                        "C1 - Parental responsibility by the father",
                        "C13A - Special guardianship order",
                        "C20 - Secure accommodation (England)"));

        underTest.sendDocumentsToCafcass(
                new AdditionalApplicationsUploadedEvent(caseData, caseDataBefore, ORDER_APPLICANT_LA));

        verify(cafcassNotificationService).sendEmail(
                eq(caseData),
                eq(Set.copyOf(documents)),
                same(ADDITIONAL_DOCUMENT),
                newDocumentDataArgumentCaptor.capture()
        );

        NewDocumentData newDocumentData = newDocumentDataArgumentCaptor.getValue();
        assertThat(newDocumentData.getEmailSubjectInfo())
                .isEqualTo("additional documents");
        assertThat(newDocumentData.getDocumentTypes())
                .isEqualTo("• C2 (With notice) - Appointment of a guardian\n"
                        + "• C13A - Special guardianship order\n"
                        + "• C20 - Secure accommodation (England)\n"
                        + "• C1 - Parental responsibility by the father\n"
                        + "• C13A - Special guardianship order\n"
                        + "• C20 - Secure accommodation (England)");
    }

    @ParameterizedTest
    @MethodSource("applicationDataParams")
    void shouldNotNotifyCafcassWhenNoAdditionalDocumentsUploaded(
            AdditionalApplicationsBundle additionalApplicationsBundle) {
        CaseData caseData = CaseData.builder()
                .id(RandomUtils.nextLong())
                .sendToCtsc("Yes")
                .additionalApplicationsBundle(wrapElements(additionalApplicationsBundle))
                .build();

        underTest.sendDocumentsToCafcass(
                new AdditionalApplicationsUploadedEvent(caseData, caseData, ORDER_APPLICANT_LA));
        verify(cafcassNotificationService, never()).sendEmail(
                any(), any(), any(), any()
        );
    }

    private static Stream<Arguments> applicationDataParams() {
        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .document(C2_DOCUMENT)
            .supplementsBundle(wrapElements(Supplement.builder().document(SUPPLEMENT_1).build()))
            .supportingEvidenceBundle(wrapElements(
                SupportingEvidenceBundle.builder().document(SUPPORTING_DOCUMENT_1).build()
            ))
            .respondents(RESPONDENTS)
            .build();

        OtherApplicationsBundle otherApplicationsBundle = OtherApplicationsBundle.builder()
            .document(OTHER_APPLICATION_DOCUMENT)
            .supplementsBundle(wrapElements(Supplement.builder().document(SUPPLEMENT_2).build()))
            .supportingEvidenceBundle(wrapElements(
                SupportingEvidenceBundle.builder().document(SUPPORTING_DOCUMENT_2).build()
            ))
            .respondents(RESPONDENTS)
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
