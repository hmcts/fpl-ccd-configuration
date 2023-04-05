package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.WorkAllocationTaskType;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.events.FurtherEvidenceUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseSummary;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingDocuments;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.PositionStatementChild;
import uk.gov.hmcts.reform.fpl.model.PositionStatementRespondent;
import uk.gov.hmcts.reform.fpl.model.SkeletonArgument;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.cafcass.CourtBundleData;
import uk.gov.hmcts.reform.fpl.model.cafcass.NewDocumentData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithDocument;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.FurtherEvidenceNotificationService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.furtherevidence.FurtherEvidenceUploadDifferenceCalculator;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;
import uk.gov.hmcts.reform.fpl.service.workallocation.WorkAllocationTaskService;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.LocalDateTime.now;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.BIRTH_CERTIFICATE;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.GUARDIAN_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.ADJOURNED;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance.IN_PERSON;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType.ALL_LAS;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType.CHILD_SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType.RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.HMCTS;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.CASE_ID;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.CONFIDENTIAL_1;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.CONFIDENTIAL_2;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.LA_USER;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.NON_CONFIDENTIAL_1;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.NON_CONFIDENTIAL_2;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.PDF_DOCUMENT_1;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.PDF_DOCUMENT_2;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithAdditionalApplicationBundle;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithApplicationDocuments;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithC2AdditionalApplicationBundle;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithConfidentialLADocuments;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithCorrespondencesByHmtcs;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithCorrespondencesByLA;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithCorrespondencesBySolicitor;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithCourtBundleList;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithHearingFurtherEvidenceBundle;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithNonConfidentialLADocuments;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithNonConfidentialPDFDocumentsSolicitor;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithNonConfidentialPDFRespondentStatementsSolicitor;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildConfidentialDocumentList;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildConfidentialDocumentList2;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildHearingFurtherEvidenceBundle;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildNonConfidentialPdfDocumentList;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildRespondentStatementsList;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildSubmittedCaseData;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.commonCaseBuilder;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.createCourtBundleList;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.createDummyApplicationDocument;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.createDummyEvidenceBundle;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.userDetailsHMCTS;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.userDetailsLA;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.userDetailsRespondentSolicitor;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.CASE_SUMMARY;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.COURT_BUNDLE;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.NEW_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.POSITION_STATEMENT_CHILD;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.POSITION_STATEMENT_RESPONDENT;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.SKELETON_ARGUMENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FurtherEvidenceUploadedEventHandlerTest {
    private static final String HMCTS_USER = "HMCTS";
    private static final String REP_USER = "REP";
    private static final String LA_USER_EMAIL = "la@examaple.com";
    private static final String LA2_USER_EMAIL = "la2@examaple.com";
    private static final String CAFCASS_EMAIL = "cafcass@examaple.com";
    private static final String SENDER_FORENAME = "The";
    private static final String SENDER_SURNAME = "Sender";
    private static final String SENDER = SENDER_FORENAME + " " + SENDER_SURNAME;
    private static final String REP_SOLICITOR_1_EMAIL = "rep_solicitor1@example.com";
    private static final String REP_SOLICITOR_2_EMAIL = "rep_solicitor2@example.com";
    private static final String REP_SOLICITOR_3_EMAIL = "rep_solicitor3@example.com";
    private static final String REP_SOLICITOR_4_EMAIL = "rep_solicitor4@example.com";
    private static final LocalDateTime HEARING_DATE = now().plusMonths(3);
    private static final CaseData CASE_DATA = mock(CaseData.class);
    private static final CaseData CASE_DATA_BEFORE = mock(CaseData.class);
    private static final LanguageTranslationRequirement TRANSLATION_REQUIREMENTS =
        LanguageTranslationRequirement.ENGLISH_TO_WELSH;
    private static final DocumentReference DOCUMENT = mock(DocumentReference.class);
    private static final List<String> NON_CONFIDENTIAL = buildNonConfidentialDocumentsNamesList();
    private static final List<String> CONFIDENTIAL = buildConfidentialDocumentsNamesList();

    @Mock
    private FurtherEvidenceNotificationService furtherEvidenceNotificationService;

    @Mock
    private TranslationRequestService translationRequestService;

    @Mock
    private FurtherEvidenceUploadDifferenceCalculator calculator;

    @Mock
    private CafcassNotificationService cafcassNotificationService;

    @InjectMocks
    private FurtherEvidenceUploadedEventHandler furtherEvidenceUploadedEventHandler;

    @Captor
    private ArgumentCaptor<CourtBundleData> courtBundleCaptor;

    @Captor
    private ArgumentCaptor<NewDocumentData> newDocumentDataCaptor;

    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private WorkAllocationTaskService workAllocationTaskService;
    @Mock
    private UserService userService;

    private static final Consumer<CaseData> EMPTY_CASE_DATA_MODIFIER = whatever -> { };

    @BeforeEach
    void beforeEach() {
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        given(userService.getCaseRoles(CASE_ID)).willReturn(Set.of(LASOLICITOR));
    }

    private void verifyNotificationFurtherDocumentsTemplate(final UserDetails uploadedBy,
                                                             DocumentUploaderType uploadedType,
                                                             Consumer<CaseData> beforeCaseDataModifier,
                                                             Consumer<CaseData> caseDataModifier,
                                                             Set<DocumentUploadNotificationUserType> notificationTypes,
                                                             List<String> expectedDocumentNames) {
        CaseData caseDataBefore = buildSubmittedCaseData();
        beforeCaseDataModifier.accept(caseDataBefore);
        CaseData caseData = buildSubmittedCaseData();
        caseDataModifier.accept(caseData);

        final Set<String> respondentSolicitors = Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_3_EMAIL);
        when(furtherEvidenceNotificationService.getRespondentSolicitorEmails(caseData))
            .thenReturn(respondentSolicitors);
        final Set<String> childSolicitors = Set.of(REP_SOLICITOR_2_EMAIL, REP_SOLICITOR_4_EMAIL);
        when(furtherEvidenceNotificationService.getChildSolicitorEmails(caseData))
            .thenReturn(childSolicitors);
        final Set<String> allLAs = Set.of(LA_USER_EMAIL, LA2_USER_EMAIL);
        when(furtherEvidenceNotificationService.getLocalAuthoritiesRecipients(caseData))
            .thenReturn(Set.of(LA_USER_EMAIL, LA2_USER_EMAIL));
        final Set<String> allCafcassEmails = Set.of(CAFCASS_EMAIL);
        when(furtherEvidenceNotificationService.getCafcassEmails(caseData))
            .thenReturn(Set.of(CAFCASS_EMAIL));

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(caseData, caseDataBefore, uploadedType, uploadedBy);
        furtherEvidenceUploadedEventHandler.sendDocumentsUploadedNotification(furtherEvidenceUploadedEvent);

        if (!notificationTypes.isEmpty()) {
            if (notificationTypes.contains(ALL_LAS)) {
                verify(furtherEvidenceNotificationService).sendNotification(
                    any(), eq(allLAs), eq(SENDER), eq(expectedDocumentNames));
            } else {
                verify(furtherEvidenceNotificationService, never()).sendNotification(any(),
                    eq(allLAs), any(), any());
            }
            if (notificationTypes.contains(CAFCASS)) {
                verify(furtherEvidenceNotificationService).sendNotification(
                    any(), eq(allCafcassEmails), eq(SENDER), eq(expectedDocumentNames));
            } else {
                verify(furtherEvidenceNotificationService, never()).sendNotification(any(),
                    eq(allCafcassEmails), any(), any());
            }
            if (notificationTypes.contains(CHILD_SOLICITOR)) {
                verify(furtherEvidenceNotificationService).sendNotification(
                    any(), eq(childSolicitors), eq(SENDER), eq(expectedDocumentNames));
            } else {
                verify(furtherEvidenceNotificationService, never()).sendNotification(any(),
                    eq(childSolicitors), any(), any());
            }
            if (notificationTypes.contains(RESPONDENT_SOLICITOR)) {
                verify(furtherEvidenceNotificationService).sendNotification(
                    any(), eq(respondentSolicitors), eq(SENDER), eq(expectedDocumentNames));
            } else {
                verify(furtherEvidenceNotificationService, never()).sendNotification(any(),
                    eq(respondentSolicitors), any(), any());
            }
        } else {
            verify(furtherEvidenceNotificationService, never()).sendNotification(any(), any(), any(), any());
        }
    }

    @Test
    void shouldNotNotifyTranslationTeamWhenNoChange() {
        when(calculator.calculate(CASE_DATA, CASE_DATA_BEFORE)).thenReturn(List.of());

        furtherEvidenceUploadedEventHandler.notifyTranslationTeam(new FurtherEvidenceUploadedEvent(CASE_DATA,
            CASE_DATA_BEFORE,
            null,
            null)
        );

        verifyNoInteractions(translationRequestService);
    }

    @Test
    void shouldNotifyTranslationTeamWhenChanges() {
        when(calculator.calculate(CASE_DATA, CASE_DATA_BEFORE)).thenReturn(List.of(
            element(UUID.randomUUID(), SupportingEvidenceBundle.builder()
                .type(FurtherEvidenceType.APPLICANT_STATEMENT)
                .name("Name")
                .dateTimeUploaded(LocalDateTime.of(2012, 1, 2, 3, 4, 5))
                .translationRequirements(TRANSLATION_REQUIREMENTS)
                .document(DOCUMENT)
                .build())
        ));

        furtherEvidenceUploadedEventHandler.notifyTranslationTeam(new FurtherEvidenceUploadedEvent(CASE_DATA,
            CASE_DATA_BEFORE,
            null,
            null)
        );

        verify(translationRequestService).sendRequest(CASE_DATA,
            Optional.of(TRANSLATION_REQUIREMENTS),
            DOCUMENT,
            "Application statement - Name - 2 January 2012");
        verifyNoMoreInteractions(translationRequestService);
    }

    @Test
    void shouldNotEmailCafcassWhenNoNewBundle() {
        String hearing = "Hearing";
        CaseData caseData = buildCaseDataWithCourtBundleList(
                2,
                hearing,
                "LA");
        CaseData caseDataBefore = commonCaseBuilder()
            .hearingDocuments(HearingDocuments.builder()
                .courtBundleList(caseData.getHearingDocuments().getCourtBundleList())
                .build())
            .build();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(
                        caseData,
                        caseDataBefore,
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA()
                );
        furtherEvidenceUploadedEventHandler.sendCourtBundlesToCafcass(furtherEvidenceUploadedEvent);

        verify(cafcassNotificationService, never()).sendEmail(eq(caseData),
                any(),
                eq(COURT_BUNDLE),
                any());
    }

    @Test
    void shouldEmailCafcassWhenNewBundleIsAdded() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
                .thenReturn(
                        Optional.of(
                                new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                        )
            );
        String hearing = "Hearing";
        CaseData caseData = buildCaseDataWithCourtBundleList(
                2,
                hearing,
                "LA");
        List<Element<HearingCourtBundle>> courtBundleList = caseData.getHearingDocuments().getCourtBundleListV2();
        Element<HearingCourtBundle> existingBundle = courtBundleList.remove(1);

        CaseData caseDataBefore = commonCaseBuilder()
            .hearingDocuments(HearingDocuments.builder()
                .courtBundleListV2(List.of(existingBundle))
                .build()
            ).build();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(
                        caseData,
                        caseDataBefore,
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA()
                );
        furtherEvidenceUploadedEventHandler.sendCourtBundlesToCafcass(furtherEvidenceUploadedEvent);
        Set<DocumentReference> documentReferences = courtBundleList.stream()
                .map(courtBundle -> courtBundle.getValue().getCourtBundle().get(0).getValue().getDocument())
                .collect(toSet());

        verify(cafcassNotificationService).sendEmail(eq(caseData),
                eq(documentReferences),
                eq(COURT_BUNDLE),
                courtBundleCaptor.capture());

        CourtBundleData courtBundleData = courtBundleCaptor.getValue();
        assertThat(courtBundleData.getHearingDetails()).isEqualTo(hearing);
    }


    @Test
    void shouldEmailCafcassWhenNewBundlesAreAdded() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
                .thenReturn(
                        Optional.of(
                                new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                        )
            );
        String hearing = "Hearing";
        String secHearing = "secHearing";
        String hearingOld = "Old";
        List<Element<HearingCourtBundle>> hearing1 = createCourtBundleList(2, hearing, "LA");
        List<Element<HearingCourtBundle>> oldHearing = createCourtBundleList(1, hearingOld, "LA");
        List<Element<HearingCourtBundle>> hearing2 = createCourtBundleList(3, hearing, "LA");
        List<Element<HearingCourtBundle>> secHearingBundle = createCourtBundleList(2, secHearing, "LA");

        List<Element<HearingCourtBundle>> totalHearing = new ArrayList<>(hearing1);
        totalHearing.addAll(oldHearing);
        totalHearing.addAll(hearing2);
        totalHearing.addAll(secHearingBundle);

        Collections.shuffle(totalHearing);

        CaseData caseData = commonCaseBuilder()
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .hearingDocuments(HearingDocuments.builder()
                .courtBundleListV2(totalHearing)
                .build())
            .build();

        CaseData caseDataBefore = commonCaseBuilder()
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .hearingDocuments(HearingDocuments.builder()
                .courtBundleListV2(oldHearing)
                .build())
            .build();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(
                        caseData,
                        caseDataBefore,
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA()
                );
        furtherEvidenceUploadedEventHandler.sendCourtBundlesToCafcass(furtherEvidenceUploadedEvent);
        List<Element<HearingCourtBundle>> expectedBundle = new ArrayList<>(hearing1);
        expectedBundle.addAll(hearing2);

        Set<DocumentReference> documentReferences = expectedBundle.stream()
                .map(courtBundle -> courtBundle.getValue().getCourtBundle().get(0).getValue().getDocument())
                .collect(toSet());

        verify(cafcassNotificationService).sendEmail(eq(caseData),
                eq(documentReferences),
                eq(COURT_BUNDLE),
                courtBundleCaptor.capture());

        CourtBundleData courtBundleData = courtBundleCaptor.getValue();
        assertThat(courtBundleData.getHearingDetails()).isEqualTo(hearing);

        Set<DocumentReference> secDocBundle = secHearingBundle.stream()
                .map(courtBundle -> courtBundle.getValue().getCourtBundle().get(0).getValue().getDocument())
                .collect(toSet());

        verify(cafcassNotificationService).sendEmail(eq(caseData),
                eq(secDocBundle),
                eq(COURT_BUNDLE),
                courtBundleCaptor.capture());

        courtBundleData = courtBundleCaptor.getValue();
        assertThat(courtBundleData.getHearingDetails()).isEqualTo(secHearing);
    }

    @Test
    void shouldEmailCafcassWhenDocsIsUploadedByLA() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
                .thenReturn(
                        Optional.of(
                                new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                        )
            );

        CaseData caseData = buildCaseDataWithNonConfidentialLADocuments();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(
                        caseData,
                        buildCaseDataWithConfidentialLADocuments(),
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA());

        furtherEvidenceUploadedEventHandler.sendDocumentsToCafcass(furtherEvidenceUploadedEvent);

        Set<DocumentReference> documentReferences = unwrapElements(caseData.getFurtherEvidenceDocumentsLA())
                .stream()
                .map(SupportingEvidenceBundle::getDocument)
                .collect(toSet());

        verify(cafcassNotificationService).sendEmail(
                eq(caseData),
                eq(documentReferences),
                eq(NEW_DOCUMENT),
                newDocumentDataCaptor.capture());

        NewDocumentData newDocumentData = newDocumentDataCaptor.getValue();
        assertThat(newDocumentData.getDocumentTypes())
                .isEqualTo("• Child's guardian reports\n"
                        + "• Child's guardian reports");
        assertThat(newDocumentData.getEmailSubjectInfo())
                .isEqualTo("Further documents for main application");
    }

    @Test
    void shouldNotEmailCafcassWhenNoticeOfActingOrIssueDocsRelatingToHearingIsUploadedByLA() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
            .thenReturn(
                Optional.of(
                    new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                )
            );

        CaseData caseData = buildCaseDataWithHearingFurtherEvidenceBundle(NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE);

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithConfidentialLADocuments(),
                DESIGNATED_LOCAL_AUTHORITY,
                userDetailsLA());

        furtherEvidenceUploadedEventHandler.sendDocumentsToCafcass(furtherEvidenceUploadedEvent);

        verify(cafcassNotificationService, never()).sendEmail(
            any(),
            any(),
            any(),
            any());
    }

    @Test
    void shouldNotEmailCafcassWhenNoticeOfActingOrIssueDocsIsUploadedByLA() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
            .thenReturn(
                Optional.of(
                    new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                )
            );

        CaseData caseData = buildCaseDataWithNonConfidentialLADocuments(NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE);

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithConfidentialLADocuments(),
                DESIGNATED_LOCAL_AUTHORITY,
                userDetailsLA());

        furtherEvidenceUploadedEventHandler.sendDocumentsToCafcass(furtherEvidenceUploadedEvent);

        verify(cafcassNotificationService, never()).sendEmail(
            any(),
            any(),
            any(),
            any());
    }

    @Test
    void shouldEmailCafcassWhenDocsIsUploadedBySolicitor() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
                .thenReturn(
                        Optional.of(
                                new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                        )
            );

        CaseData caseData = buildCaseDataWithNonConfidentialPDFDocumentsSolicitor(REP_USER);

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(
                        caseData,
                        buildCaseDataWithConfidentialLADocuments(),
                        SOLICITOR,
                        userDetailsRespondentSolicitor());

        furtherEvidenceUploadedEventHandler.sendDocumentsToCafcass(furtherEvidenceUploadedEvent);

        Set<DocumentReference> documentReferences = unwrapElements(caseData.getFurtherEvidenceDocumentsSolicitor())
                .stream()
                .map(SupportingEvidenceBundle::getDocument)
                .collect(toSet());

        verify(cafcassNotificationService).sendEmail(
                eq(caseData),
                eq(documentReferences),
                eq(NEW_DOCUMENT),
                newDocumentDataCaptor.capture());

        NewDocumentData newDocumentData = newDocumentDataCaptor.getValue();
        assertThat(newDocumentData.getDocumentTypes())
                .contains("• non-confidential-1");
        assertThat(newDocumentData.getDocumentTypes())
                .contains("• non-confidential-2");

        assertThat(newDocumentData.getEmailSubjectInfo())
                .isEqualTo("Further documents for main application");
    }

    @Test
    void shouldEmailCafcassWhenGuardianReportsAreUploadedBySolicitor() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
            .thenReturn(
                Optional.of(
                    new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                )
            );

        CaseData caseData = buildCaseDataWithNonConfidentialPDFDocumentsSolicitor(REP_USER, GUARDIAN_REPORTS);

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithConfidentialLADocuments(),
                SOLICITOR,
                userDetailsRespondentSolicitor());

        furtherEvidenceUploadedEventHandler.sendDocumentsToCafcass(furtherEvidenceUploadedEvent);

        Set<DocumentReference> documentReferences = unwrapElements(caseData.getFurtherEvidenceDocumentsSolicitor())
            .stream()
            .map(SupportingEvidenceBundle::getDocument)
            .collect(toSet());

        verify(cafcassNotificationService).sendEmail(
            eq(caseData),
            eq(documentReferences),
            eq(NEW_DOCUMENT),
            newDocumentDataCaptor.capture());

        NewDocumentData newDocumentData = newDocumentDataCaptor.getValue();
        assertThat(newDocumentData.getDocumentTypes())
            .contains("• Child's guardian reports");
        assertThat(newDocumentData.getDocumentTypes())
            .contains("• Child's guardian reports");
        assertThat(newDocumentData.getEmailSubjectInfo())
            .isEqualTo("Further documents for main application");
    }

    @Test
    void shouldNotEmailCafcassWhenNoticeOfActingOrIssueIsUploadedBySolicitor() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
            .thenReturn(
                Optional.of(
                    new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                )
            );
        CaseData caseData = buildCaseDataWithNonConfidentialPDFDocumentsSolicitor(REP_USER,
            NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE);

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                buildCaseDataWithConfidentialLADocuments(),
                SOLICITOR,
                userDetailsRespondentSolicitor());

        furtherEvidenceUploadedEventHandler.sendDocumentsToCafcass(furtherEvidenceUploadedEvent);

        verify(cafcassNotificationService, never()).sendEmail(
            any(),
            any(),
            any(),
            any());
    }

    @Test
    void shouldEmailCafcassWhenAdditionalBundleIsUploadedByLA() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
                .thenReturn(
                        Optional.of(
                                new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                        )
            );


        CaseData caseData = buildCaseDataWithAdditionalApplicationBundle();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(
                        caseData,
                        buildCaseDataWithConfidentialLADocuments(),
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA());

        furtherEvidenceUploadedEventHandler.sendDocumentsToCafcass(furtherEvidenceUploadedEvent);

        Set<DocumentReference> documentReferences = unwrapElements(caseData.getAdditionalApplicationsBundle())
                .stream()
                .map(AdditionalApplicationsBundle::getOtherApplicationsBundle)
                .map(OtherApplicationsBundle::getAllDocumentReferences)
                .flatMap(List::stream)
                .map(Element::getValue)
                .collect(toSet());


        verify(cafcassNotificationService).sendEmail(
                eq(caseData),
                eq(documentReferences),
                eq(NEW_DOCUMENT),
                newDocumentDataCaptor.capture());

        NewDocumentData newDocumentData = newDocumentDataCaptor.getValue();
        assertThat(newDocumentData.getDocumentTypes())
                .contains("• additional applications");
        assertThat(newDocumentData.getDocumentTypes())
                .contains("• additional applications");

        assertThat(newDocumentData.getEmailSubjectInfo())
                .isEqualTo("additional applications");
    }

    @Test
    void shouldEmailCafcassWhenC2AdditionalBundleIsUploadedByLA() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
                .thenReturn(
                        Optional.of(
                                new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                        )
            );


        CaseData caseData = buildCaseDataWithC2AdditionalApplicationBundle();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(
                        caseData,
                        buildCaseDataWithConfidentialLADocuments(),
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA());

        furtherEvidenceUploadedEventHandler.sendDocumentsToCafcass(furtherEvidenceUploadedEvent);

        Set<DocumentReference> documentReferences = unwrapElements(caseData.getAdditionalApplicationsBundle())
                .stream()
                .map(AdditionalApplicationsBundle::getC2DocumentBundle)
                .map(C2DocumentBundle::getAllC2DocumentReferences)
                .flatMap(List::stream)
                .map(Element::getValue)
                .collect(toSet());


        verify(cafcassNotificationService).sendEmail(
                eq(caseData),
                eq(documentReferences),
                eq(NEW_DOCUMENT),
                newDocumentDataCaptor.capture());

        NewDocumentData newDocumentData = newDocumentDataCaptor.getValue();
        assertThat(newDocumentData.getDocumentTypes())
                .contains("• additional applications");
        assertThat(newDocumentData.getDocumentTypes())
                .contains("• additional applications");

        assertThat(newDocumentData.getEmailSubjectInfo())
                .isEqualTo("additional applications");
    }

    @Test
    void shouldEmailCafcassWhenRespondentStatementIsUploaded() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
                .thenReturn(
                        Optional.of(
                                new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                        )
            );

        CaseData caseData = buildCaseDataWithNonConfidentialPDFRespondentStatementsSolicitor();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(
                        caseData,
                        buildCaseDataWithConfidentialLADocuments(),
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA());

        furtherEvidenceUploadedEventHandler.sendDocumentsToCafcass(furtherEvidenceUploadedEvent);

        Set<DocumentReference> documentReferences = unwrapElements(caseData.getRespondentStatements())
                .stream()
                .flatMap(statement -> unwrapElements(statement.getSupportingEvidenceBundle()).stream())
                .map(SupportingEvidenceBundle::getDocument)
                .collect(toSet());

        verify(cafcassNotificationService).sendEmail(
                eq(caseData),
                eq(documentReferences),
                eq(NEW_DOCUMENT),
                newDocumentDataCaptor.capture());

        NewDocumentData newDocumentData = newDocumentDataCaptor.getValue();
        assertThat(newDocumentData.getDocumentTypes())
                .isEqualTo("• Respondent statement");
        assertThat(newDocumentData.getEmailSubjectInfo())
                .isEqualTo("Further documents for main application");
    }

    @Test
    void shouldEmailCafcassWhenHearingFurtherEvidenceBundleIsUploaded() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
                .thenReturn(
                        Optional.of(
                                new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                        )
            );

        CaseData caseData = buildCaseDataWithHearingFurtherEvidenceBundle();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(
                        caseData,
                        buildCaseDataWithConfidentialLADocuments(),
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA());

        furtherEvidenceUploadedEventHandler.sendDocumentsToCafcass(furtherEvidenceUploadedEvent);

        Set<DocumentReference> documentReferences = unwrapElements(
                caseData.getHearingFurtherEvidenceDocuments()).stream()
                    .map(HearingFurtherEvidenceBundle::getSupportingEvidenceBundle)
                    .flatMap(List::stream)
                    .map(Element::getValue)
                    .map(SupportingEvidenceBundle::getDocument)
                    .collect(Collectors.toSet());

        verify(cafcassNotificationService).sendEmail(
            eq(caseData),
            eq(documentReferences),
            eq(NEW_DOCUMENT),
            newDocumentDataCaptor.capture());

        NewDocumentData newDocumentData = newDocumentDataCaptor.getValue();
        assertThat(newDocumentData.getDocumentTypes())
                .isEqualTo("• Child's guardian reports\n"
                        + "• Child's guardian reports");
        assertThat(newDocumentData.getEmailSubjectInfo())
                .isEqualTo("Further documents for main application");
    }

    @Test
    void shouldEmailCafcassWhenApplicationDocumentIsUploaded() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
                .thenReturn(
                        Optional.of(
                                new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                        )
            );

        CaseData caseData = buildCaseDataWithApplicationDocuments();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(
                        caseData,
                        buildCaseDataWithConfidentialLADocuments(),
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA());

        furtherEvidenceUploadedEventHandler.sendDocumentsToCafcass(furtherEvidenceUploadedEvent);

        Set<DocumentReference> documentReferences = unwrapElements(caseData.getApplicationDocuments())
                .stream()
                .map(WithDocument::getDocument)
                .collect(toSet());

        verify(cafcassNotificationService).sendEmail(
                eq(caseData),
                eq(documentReferences),
                eq(NEW_DOCUMENT),
                newDocumentDataCaptor.capture());

        NewDocumentData newDocumentData = newDocumentDataCaptor.getValue();
        assertThat(newDocumentData.getDocumentTypes())
                .isEqualTo("• Birth certificate");
        assertThat(newDocumentData.getEmailSubjectInfo())
                .isEqualTo("Further documents for main application");
    }

    @Test
    void shouldNotEmailCafcassWhenNoApplicationDocumentIsUploaded() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
                .thenReturn(
                        Optional.of(
                                new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                        )
            );

        CaseData caseData = buildCaseDataWithApplicationDocuments();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(
                        caseData,
                        caseData,
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA());

        furtherEvidenceUploadedEventHandler.sendDocumentsToCafcass(furtherEvidenceUploadedEvent);

        Set<DocumentReference> documentReferences = unwrapElements(caseData.getApplicationDocuments())
                .stream()
                .map(WithDocument::getDocument)
                .collect(toSet());

        verify(cafcassNotificationService, never()).sendEmail(
                any(),
                any(),
                any(),
                any());
    }

    @Test
    void shouldNotSendEmailToCafcassWhenRespondentStatementIsUploaded() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
                .thenReturn(
                        Optional.of(
                                new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                        )
            );

        CaseData caseData = buildCaseDataWithNonConfidentialPDFRespondentStatementsSolicitor();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(
                        caseData,
                        caseData,

                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA());

        furtherEvidenceUploadedEventHandler.sendDocumentsToCafcass(furtherEvidenceUploadedEvent);

        verify(cafcassNotificationService, never()).sendEmail(
                any(),
                any(),
                any(),
                any());
    }

    @Test
    void shouldNotSendEmailToCafcassWhenCafcassIsNotEnlang() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
                .thenReturn(
                        Optional.empty()
            );

        CaseData caseData = buildCaseDataWithNonConfidentialPDFRespondentStatementsSolicitor();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(
                        caseData,
                        caseData,
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA());

        furtherEvidenceUploadedEventHandler.sendDocumentsToCafcass(furtherEvidenceUploadedEvent);

        verify(cafcassNotificationService, never()).sendEmail(
                any(),
                any(),
                any(),
                any());
    }

    @Test
    void shouldEmailCafcassWhendCaseDataWithCorrespondencesIsUploadedByHmtcs() {
        CaseData caseData = buildCaseDataWithCorrespondencesByHmtcs();
        verifyCorresspondences(caseData,  caseData.getCorrespondenceDocuments());
    }

    @Test
    void shouldEmailCafcassWhendCaseDataWithCorrespondencesIsUploadedByLA() {
        CaseData caseData = buildCaseDataWithCorrespondencesByLA();
        verifyCorresspondences(caseData,  caseData.getCorrespondenceDocumentsLA());
    }

    @Test
    void shouldEmailCafcassWhendCaseDataWithCorrespondencesIsUploadedBySolicitor() {
        CaseData caseData = buildCaseDataWithCorrespondencesBySolicitor();
        verifyCorresspondences(caseData,  caseData.getCorrespondenceDocumentsSolicitor());
    }

    private void verifyCorresspondences(CaseData caseData, List<Element<SupportingEvidenceBundle>> correspondence) {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
                .thenReturn(
                        Optional.of(
                                new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                        )
            );

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(
                        caseData,
                        buildCaseDataWithConfidentialLADocuments(),
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA());

        furtherEvidenceUploadedEventHandler.sendDocumentsToCafcass(furtherEvidenceUploadedEvent);

        Set<DocumentReference> documentReferences = unwrapElements(correspondence)
                .stream()
                .map(SupportingEvidenceBundle::getDocument)
                .collect(toSet());

        verify(cafcassNotificationService).sendEmail(
                eq(caseData),
                eq(documentReferences),
                eq(NEW_DOCUMENT),
                newDocumentDataCaptor.capture());

        NewDocumentData newDocumentData = newDocumentDataCaptor.getValue();
        assertThat(newDocumentData.getDocumentTypes())
                .isEqualTo("• Correspondence");
        assertThat(newDocumentData.getEmailSubjectInfo())
                .isEqualTo("Correspondence");
    }


    @Test
    void shouldNotSendEmailToCafcassWhenNoNewDocIsUploadedByLA() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
                .thenReturn(
                        Optional.of(
                                new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                        )
            );

        CaseData caseData = buildCaseDataWithNonConfidentialLADocuments();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(
                        caseData,
                        caseData,
                        DESIGNATED_LOCAL_AUTHORITY,
                        userDetailsLA());

        furtherEvidenceUploadedEventHandler.sendDocumentsToCafcass(furtherEvidenceUploadedEvent);

        verify(cafcassNotificationService, never()).sendEmail(
                any(),
                any(),
                any(),
                any());
    }

    private void verifyNotificationForCourtBundleTemplate(final UserDetails uploadedBy,
                                                            DocumentUploaderType uploadedType,
                                                            Consumer<CaseData> beforeCaseDataModifier,
                                                            Consumer<CaseData> caseDataModifier,
                                                            List<String> expectedHearingDetails) {
        CaseData caseDataBefore = buildSubmittedCaseData();
        beforeCaseDataModifier.accept(caseDataBefore);
        CaseData caseData = buildSubmittedCaseData();
        caseDataModifier.accept(caseData);
        boolean isHavingNotification = expectedHearingDetails != null && !expectedHearingDetails.isEmpty();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                caseDataBefore,
                uploadedType,
                uploadedBy);

        if (isHavingNotification) {
            when(furtherEvidenceNotificationService.getChildSolicitorEmails(caseData))
                .thenReturn(Set.of(REP_SOLICITOR_1_EMAIL));
            when(furtherEvidenceNotificationService.getRespondentSolicitorEmails(caseData))
                .thenReturn(Set.of(REP_SOLICITOR_2_EMAIL));
            when(furtherEvidenceNotificationService.getDesignatedLocalAuthorityRecipients(caseData))
                .thenReturn(Set.of(LA_USER_EMAIL));
            when(furtherEvidenceNotificationService.getLocalAuthoritiesRecipients(caseData))
                .thenReturn(Set.of(LA2_USER_EMAIL));
        }

        furtherEvidenceUploadedEventHandler.sendCourtBundlesUploadedNotification(furtherEvidenceUploadedEvent);

        if (isHavingNotification) {
            for (String hearingDetail : expectedHearingDetails) {
                verify(furtherEvidenceNotificationService).sendNotificationForCourtBundleUploaded(caseData,
                    Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL, LA_USER_EMAIL, LA2_USER_EMAIL), hearingDetail);
            }
        } else {
            verify(furtherEvidenceNotificationService, never()).sendNotificationForCourtBundleUploaded(any(),
                any(), any());
        }
    }

    @Test
    void shouldSendNotificationWhenCourtBundleIsUploadedByLA() {
        String hearing1 = "1stHearing";
        String hearing2 = "2ndHearing";
        String hearing3 = "3rdHearing";
        List<Element<HearingCourtBundle>> firstHearingBundle = createCourtBundleList(2, hearing1, "LA");
        List<Element<HearingCourtBundle>> secondHearingBundle = createCourtBundleList(2, hearing2, "LA");
        List<Element<HearingCourtBundle>> thirdHearingBundle = createCourtBundleList(2, hearing3, "LA");

        List<Element<HearingCourtBundle>> totalHearing = new ArrayList<>();
        totalHearing.addAll(firstHearingBundle);
        totalHearing.addAll(secondHearingBundle);
        totalHearing.addAll(thirdHearingBundle);

        Collections.shuffle(totalHearing);

        verifyNotificationForCourtBundleTemplate(
            userDetailsLA(), DESIGNATED_LOCAL_AUTHORITY, EMPTY_CASE_DATA_MODIFIER,
            (caseData) -> caseData.getHearingDocuments().getCourtBundleListV2().addAll(totalHearing),
            List.of(hearing1, hearing2, hearing3));
    }

    @Test
    void shouldSendNotificationWhenHearingDocumentsIsUploaded() {
        final List<Element<HearingBooking>> hearingBooking = wrapElements(testHearing());
        final String hearingBookingLabel = hearingBooking.get(0).getValue().toLabel();
        final List<Element<CaseSummary>> caseSummaryList = wrapElements(
            CaseSummary.builder().hearing(hearingBookingLabel)
                .document(TestDataHelper.testDocumentReference("CaseSummary 1.pdf")).build(),
            CaseSummary.builder().hearing(hearingBookingLabel)
                .document(TestDataHelper.testDocumentReference("CaseSummary 2.pdf")).build());
        final List<Element<PositionStatementChild>> positionStatementChildList = wrapElements(
            PositionStatementChild.builder()
                .hearing(hearingBookingLabel)
                .document(TestDataHelper.testDocumentReference("PositionStatementChild.pdf")).build());
        final List<Element<PositionStatementRespondent>> positionStatementRespondentList = wrapElements(
            PositionStatementRespondent.builder()
                .hearing(hearingBookingLabel)
                .document(TestDataHelper.testDocumentReference("PositionStatementRespondent.pdf")).build());
        final List<Element<SkeletonArgument>> skeletonArgumentList = wrapElements(
            SkeletonArgument.builder()
                .hearing(hearingBookingLabel)
                .document(TestDataHelper.testDocumentReference("SkeletonArgument.pdf")).build());


        CaseData caseDataBefore = buildSubmittedCaseData();
        CaseData caseData = buildSubmittedCaseData().toBuilder()
            .hearingDetails(hearingBooking)
            .hearingDocuments(HearingDocuments.builder()
                .caseSummaryList(caseSummaryList)
                .skeletonArgumentList(skeletonArgumentList)
                .positionStatementChildList(positionStatementChildList)
                .positionStatementRespondentList(positionStatementRespondentList).build())
            .build();

        final Set<String> respondentSolicitors = Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_3_EMAIL);
        when(furtherEvidenceNotificationService.getRespondentSolicitorEmails(caseData))
            .thenReturn(respondentSolicitors);
        final Set<String> childSolicitors = Set.of(REP_SOLICITOR_2_EMAIL, REP_SOLICITOR_4_EMAIL);
        when(furtherEvidenceNotificationService.getChildSolicitorEmails(caseData))
            .thenReturn(childSolicitors);
        final Set<String> allLAs = Set.of(LA_USER_EMAIL, LA2_USER_EMAIL);
        when(furtherEvidenceNotificationService.getLocalAuthoritiesRecipients(caseData))
            .thenReturn(allLAs);

        UserDetails userDetails = userDetailsLA();
        FurtherEvidenceUploadedEvent event = new FurtherEvidenceUploadedEvent(caseData, caseDataBefore, SOLICITOR,
            userDetails);

        furtherEvidenceUploadedEventHandler.sendHearingDocumentsUploadedNotification(event);

        final Set<String> expectedRecipients = new HashSet<>();
        expectedRecipients.addAll(respondentSolicitors);
        expectedRecipients.addAll(childSolicitors);
        expectedRecipients.addAll(allLAs);

        List<String> expectedNewDocumentName =
            List.of("CaseSummary 1.pdf", "CaseSummary 2.pdf", "PositionStatementChild.pdf",
                "PositionStatementRespondent.pdf", "SkeletonArgument.pdf");

        verify(furtherEvidenceNotificationService)
            .sendNotificationWithHearing(caseData, expectedRecipients, userDetails.getFullName(),
                expectedNewDocumentName, Optional.of(caseData.getHearingDetails().get(0).getValue()));
    }

    @Test
    void shouldEmailCafcassWhenNewHearingDocumentAdded() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
            .thenReturn(
                Optional.of(
                    new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                )
            );

        String hearing = "Hearing";
        final DocumentReference caseSummaryDoc1 = TestDataHelper.testDocumentReference("CaseSummary 1.pdf");
        final DocumentReference caseSummaryDoc2 = TestDataHelper.testDocumentReference("CaseSummary 2.pdf");
        final DocumentReference positionStatementChildDoc =
            TestDataHelper.testDocumentReference("PositionStatementChild.pdf");
        final DocumentReference positionStatementRespondentDoc =
            TestDataHelper.testDocumentReference("PositionStatementRespondent.pdf");
        final DocumentReference skeletonArgumentDoc =
            TestDataHelper.testDocumentReference("SkeletonArgument.pdf");

        final List<Element<CaseSummary>> caseSummaryList = wrapElements(
            CaseSummary.builder().hearing(hearing).document(caseSummaryDoc1).build(),
            CaseSummary.builder().hearing(hearing).document(caseSummaryDoc2).build());
        final List<Element<PositionStatementChild>> positionStatementChildList = wrapElements(
            PositionStatementChild.builder().hearing(hearing).document(positionStatementChildDoc).build());
        final List<Element<PositionStatementRespondent>> positionStatementRespondentList = wrapElements(
            PositionStatementRespondent.builder().hearing(hearing).document(positionStatementRespondentDoc).build());
        final List<Element<SkeletonArgument>> skeletonArgumentList = wrapElements(
            SkeletonArgument.builder().hearing(hearing).document(skeletonArgumentDoc).build());

        CaseData caseDataBefore = buildSubmittedCaseData();
        CaseData caseData = buildSubmittedCaseData().toBuilder()
            .hearingDocuments(HearingDocuments.builder()
                .caseSummaryList(caseSummaryList)
                .skeletonArgumentList(skeletonArgumentList)
                .positionStatementChildList(positionStatementChildList)
                .positionStatementRespondentList(positionStatementRespondentList).build())
            .build();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                caseDataBefore,
                DESIGNATED_LOCAL_AUTHORITY,
                userDetailsLA()
            );
        furtherEvidenceUploadedEventHandler.sendHearingDocumentsToCafcass(furtherEvidenceUploadedEvent);

        CourtBundleData expectedCourtBundleData = CourtBundleData.builder().hearingDetails(hearing).build();

        verify(cafcassNotificationService).sendEmail(caseData, Set.of(caseSummaryDoc1, caseSummaryDoc2),
            CASE_SUMMARY, expectedCourtBundleData);
        verify(cafcassNotificationService).sendEmail(caseData, Set.of(positionStatementChildDoc),
            POSITION_STATEMENT_CHILD, expectedCourtBundleData);
        verify(cafcassNotificationService).sendEmail(caseData, Set.of(positionStatementRespondentDoc),
            POSITION_STATEMENT_RESPONDENT, expectedCourtBundleData);
        verify(cafcassNotificationService).sendEmail(caseData, Set.of(skeletonArgumentDoc),
            SKELETON_ARGUMENT, expectedCourtBundleData);
    }

    @Test
    void shouldNotEmailCafcassWhenNoNewHearingDocument() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
            .thenReturn(
                Optional.of(
                    new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                )
            );

        String hearing = "Hearing";
        final DocumentReference caseSummaryDoc1 = TestDataHelper.testDocumentReference("CaseSummary 1.pdf");
        final DocumentReference caseSummaryDoc2 = TestDataHelper.testDocumentReference("CaseSummary 2.pdf");
        final DocumentReference positionStatementChildDoc =
            TestDataHelper.testDocumentReference("PositionStatementChild.pdf");
        final DocumentReference positionStatementRespondentDoc =
            TestDataHelper.testDocumentReference("PositionStatementRespondent.pdf");
        final DocumentReference skeletonArgumentDoc =
            TestDataHelper.testDocumentReference("SkeletonArgument.pdf");

        final List<Element<CaseSummary>> caseSummaryList = wrapElements(
            CaseSummary.builder().hearing(hearing).document(caseSummaryDoc1).build(),
            CaseSummary.builder().hearing(hearing).document(caseSummaryDoc2).build());
        final List<Element<PositionStatementChild>> positionStatementChildList = wrapElements(
            PositionStatementChild.builder().hearing(hearing).document(positionStatementChildDoc).build());
        final List<Element<PositionStatementRespondent>> positionStatementRespondentList = wrapElements(
            PositionStatementRespondent.builder().hearing(hearing).document(positionStatementRespondentDoc).build());
        final List<Element<SkeletonArgument>> skeletonArgumentList = wrapElements(
            SkeletonArgument.builder().hearing(hearing).document(skeletonArgumentDoc).build());

        CaseData caseDataBefore = buildSubmittedCaseData().toBuilder()
            .hearingDocuments(HearingDocuments.builder()
                .caseSummaryList(caseSummaryList)
                .skeletonArgumentList(skeletonArgumentList)
                .positionStatementChildList(positionStatementChildList)
                .positionStatementRespondentList(positionStatementRespondentList).build())
            .build();
        CaseData caseData = buildSubmittedCaseData().toBuilder()
            .hearingDocuments(HearingDocuments.builder()
                .skeletonArgumentList(skeletonArgumentList)
                .caseSummaryList(caseSummaryList)
                .positionStatementChildList(positionStatementChildList)
                .positionStatementRespondentList(positionStatementRespondentList).build())
            .build();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                caseDataBefore,
                DESIGNATED_LOCAL_AUTHORITY,
                userDetailsLA()
            );
        furtherEvidenceUploadedEventHandler.sendHearingDocumentsToCafcass(furtherEvidenceUploadedEvent);

        verify(cafcassNotificationService, never()).sendEmail(eq(caseData),
            any(),
            eq(CASE_SUMMARY),
            any());
        verify(cafcassNotificationService, never()).sendEmail(eq(caseData),
            any(),
            eq(POSITION_STATEMENT_CHILD),
            any());
        verify(cafcassNotificationService, never()).sendEmail(eq(caseData),
            any(),
            eq(POSITION_STATEMENT_RESPONDENT),
            any());
        verify(cafcassNotificationService, never()).sendEmail(eq(caseData),
            any(),
            eq(SKELETON_ARGUMENT),
            any());
    }

    @Test
    void shouldNotSendNotificationWhenNoNewCourtBundleIsUploadedByLA() {
        String hearing1 = "1stHearing";
        String hearing2 = "2ndHearing";
        String hearing3 = "3rdHearing";
        List<Element<HearingCourtBundle>> firstHearingBundle = createCourtBundleList(2, hearing1, "LA");
        List<Element<HearingCourtBundle>> secondHearingBundle = createCourtBundleList(2, hearing2, "LA");
        List<Element<HearingCourtBundle>> thirdHearingBundle = createCourtBundleList(2, hearing3, "LA");

        List<Element<HearingCourtBundle>> totalHearing = new ArrayList<>();
        totalHearing.addAll(firstHearingBundle);
        totalHearing.addAll(secondHearingBundle);
        totalHearing.addAll(thirdHearingBundle);

        Collections.shuffle(totalHearing);

        verifyNotificationForCourtBundleTemplate(
            userDetailsLA(),
            DESIGNATED_LOCAL_AUTHORITY,
            (caseData) -> caseData.getHearingDocuments().getCourtBundleListV2().addAll(totalHearing),
            (caseData) -> caseData.getHearingDocuments().getCourtBundleListV2().addAll(totalHearing),
            emptyList());
    }

    @Test
    void shouldNotSendNotificationWhenNoCourtBundleIsUploadedByLA() {
        verifyNotificationForCourtBundleTemplate(
                userDetailsLA(),
                DESIGNATED_LOCAL_AUTHORITY,
                EMPTY_CASE_DATA_MODIFIER,
                EMPTY_CASE_DATA_MODIFIER,
                emptyList());
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

    private static List<String> buildNonConfidentialDocumentsNamesList() {
        return List.of(NON_CONFIDENTIAL_1, NON_CONFIDENTIAL_2);
    }

    private static List<String> buildConfidentialDocumentsNamesList() {
        return List.of(CONFIDENTIAL_1, CONFIDENTIAL_2);
    }

    @Test
    void shouldCreateWorkAllocationTaskWhenNewCorrespondenceIsAddedByLA() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
            .thenReturn(
                Optional.of(
                    new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                )
            );
        CaseData caseData = buildCaseDataWithCorrespondencesByLA();
        CaseData caseDataBefore = commonCaseBuilder().build();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                caseDataBefore,
                DESIGNATED_LOCAL_AUTHORITY,
                userDetailsLA()
            );
        furtherEvidenceUploadedEventHandler.createWorkAllocationTask(furtherEvidenceUploadedEvent);

        verify(workAllocationTaskService).createWorkAllocationTask(caseData,
            WorkAllocationTaskType.CORRESPONDENCE_UPLOADED);
    }

    @Test
    void shouldCreateWorkAllocationTaskWhenNewCorrespondenceIsAddedBySolicitor() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
            .thenReturn(
                Optional.of(
                    new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                )
            );
        CaseData caseData = buildCaseDataWithCorrespondencesBySolicitor();
        CaseData caseDataBefore = commonCaseBuilder().build();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                caseDataBefore,
                DESIGNATED_LOCAL_AUTHORITY,
                userDetailsLA()
            );
        furtherEvidenceUploadedEventHandler.createWorkAllocationTask(furtherEvidenceUploadedEvent);

        verify(workAllocationTaskService).createWorkAllocationTask(caseData,
            WorkAllocationTaskType.CORRESPONDENCE_UPLOADED);
    }

    private static Stream<Arguments> createWorkAllocationByHmctsParam() {
        return Stream.of(
            Arguments.of(true, false, true),
            Arguments.of(false, false, false),
            Arguments.of(false, true, true)
        );
    }

    @ParameterizedTest
    @MethodSource("createWorkAllocationByHmctsParam")
    void createWorkAllocationTaskWhenNewCorrespondenceIsAddedByHmcts(boolean isCafcassUser,
                                                                     boolean isJudiciaryUser,
                                                                     boolean shouldSend) {
        given(userService.isCafcassUser()).willReturn(isCafcassUser);
        given(userService.isJudiciaryUser()).willReturn(isJudiciaryUser);
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
            .thenReturn(
                Optional.of(
                    new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                )
            );
        CaseData caseData = buildCaseDataWithCorrespondencesByHmtcs();
        CaseData caseDataBefore = commonCaseBuilder().build();

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                caseDataBefore,
                DESIGNATED_LOCAL_AUTHORITY,
                userDetailsLA()
            );
        furtherEvidenceUploadedEventHandler.createWorkAllocationTask(furtherEvidenceUploadedEvent);

        verify(workAllocationTaskService, times(shouldSend ? 1 : 0)).createWorkAllocationTask(caseData,
            WorkAllocationTaskType.CORRESPONDENCE_UPLOADED);
    }

    private static UserDetails getUserDetails(DocumentUploaderType uploaderType) {
        switch (uploaderType) {
            case DESIGNATED_LOCAL_AUTHORITY:
                return userDetailsLA();
            case HMCTS:
                return userDetailsHMCTS();
            case SOLICITOR:
                return userDetailsRespondentSolicitor();
            default:
                throw new AssertionError("unexpected uploaderType");
        }
    }

    static class RespondentStatementConfidentialChangeArgs implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                of(userDetailsLA(), DESIGNATED_LOCAL_AUTHORITY, LA_USER, true, true),
                of(userDetailsLA(), DESIGNATED_LOCAL_AUTHORITY, LA_USER, true, false),
                of(userDetailsLA(), DESIGNATED_LOCAL_AUTHORITY, LA_USER, false, true),
                of(userDetailsLA(), DESIGNATED_LOCAL_AUTHORITY, LA_USER, false, false),
                of(userDetailsHMCTS(), HMCTS, HMCTS_USER, true, true),
                of(userDetailsHMCTS(), HMCTS, HMCTS_USER, true, false),
                of(userDetailsHMCTS(), HMCTS, HMCTS_USER, false, true),
                of(userDetailsHMCTS(), HMCTS, HMCTS_USER, false, false)
            );
        }
    }

    static class RespondentStatementArgs implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                of(userDetailsLA(), DESIGNATED_LOCAL_AUTHORITY, LA_USER,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR, CAFCASS), NON_CONFIDENTIAL, false),
                of(userDetailsLA(), DESIGNATED_LOCAL_AUTHORITY, LA_USER,
                    Set.of(ALL_LAS), CONFIDENTIAL, true),
                of(userDetailsHMCTS(), HMCTS, HMCTS_USER,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR, CAFCASS), NON_CONFIDENTIAL, false),
                of(userDetailsHMCTS(), HMCTS, HMCTS_USER,
                    Set.of(), CONFIDENTIAL, true),
                of(userDetailsRespondentSolicitor(), SOLICITOR, REP_USER,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR, CAFCASS), NON_CONFIDENTIAL, false)
            // Note: Respondent/Child Solicitor cannot upload confidential document
            );
        }
    }

    @Nested
    class RespondentStatement {

        @ParameterizedTest
        @ArgumentsSource(RespondentStatementArgs.class)
        void shouldSendNotificationForNewUpload(UserDetails userDetails,
                                                DocumentUploaderType uploadedType,
                                                String uploadedBy,
                                                Set<DocumentUploadNotificationUserType> notificationTypes,
                                                List<String> expectedDocumentNames,
                                                boolean confidential) {
            verifyNotificationFurtherDocumentsTemplate(
                userDetails, uploadedType, EMPTY_CASE_DATA_MODIFIER,
                (caseData) ->  caseData.getRespondentStatements().addAll(
                    buildRespondentStatementsList(
                        confidential
                            ? buildConfidentialDocumentList(uploadedBy)
                            : buildNonConfidentialPdfDocumentList(uploadedBy)
                    )),
                notificationTypes, expectedDocumentNames);
        }

        @ParameterizedTest
        @ArgumentsSource(RespondentStatementArgs.class)
        void shouldSendNotificationForUpdatingWhenReplacingDocument(
            UserDetails userDetails,
            DocumentUploaderType uploadedType,
            String uploadedBy,
            Set<DocumentUploadNotificationUserType> notificationTypes,
            List<String> expectedDocumentNames,
            boolean confidential) {
            verifyNotificationFurtherDocumentsTemplate(
                userDetails, uploadedType,
                (caseData) ->  caseData.getRespondentStatements().addAll(
                    buildRespondentStatementsList(buildConfidentialDocumentList2(uploadedBy))),
                (caseData) ->  caseData.getRespondentStatements().addAll(
                    buildRespondentStatementsList(
                        confidential
                            ? buildConfidentialDocumentList(uploadedBy)
                            : buildNonConfidentialPdfDocumentList(uploadedBy)
                    )),
                notificationTypes, expectedDocumentNames);
        }

        @ParameterizedTest
        @ArgumentsSource(RespondentStatementConfidentialChangeArgs.class)
        void shouldNotSendNotificationForConfidentialChangeOnly(UserDetails userDetails,
                                                                DocumentUploaderType uploadedType,
                                                                String uploadedBy,
                                                                boolean oldConfidential,
                                                                boolean newConfidential) {
            verifyNotificationFurtherDocumentsTemplate(
                userDetails, uploadedType,
                (caseData) ->  caseData.getRespondentStatements().addAll(
                    buildRespondentStatementsList(wrapElements(
                        createDummyEvidenceBundle(CONFIDENTIAL_1, uploadedBy, oldConfidential, PDF_DOCUMENT_1)))),
                (caseData) ->  caseData.getRespondentStatements().addAll(
                    buildRespondentStatementsList(wrapElements(
                        createDummyEvidenceBundle(CONFIDENTIAL_1, uploadedBy, newConfidential, PDF_DOCUMENT_1)))),
                Set.of(), null);
        }
    }

    static class ApplicationDocumentArgs implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                of(userDetailsLA(), DESIGNATED_LOCAL_AUTHORITY, LA_USER,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR, CAFCASS),
                    List.of(BIRTH_CERTIFICATE.getLabel()), false),
                of(userDetailsLA(), DESIGNATED_LOCAL_AUTHORITY, LA_USER,
                    Set.of(ALL_LAS),
                    List.of(BIRTH_CERTIFICATE.getLabel()), true)
            // Note: Only LAs can upload application documents
            );
        }
    }

    static class ApplicationDocumentConfidentialChangeArgs implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                of(userDetailsLA(), DESIGNATED_LOCAL_AUTHORITY, LA_USER, true, true),
                of(userDetailsLA(), DESIGNATED_LOCAL_AUTHORITY, LA_USER, true, false),
                of(userDetailsLA(), DESIGNATED_LOCAL_AUTHORITY, LA_USER, false, true),
                of(userDetailsLA(), DESIGNATED_LOCAL_AUTHORITY, LA_USER, false, false)
            );
        }
    }

    @Nested
    class ApplicationDocument {

        @ParameterizedTest
        @ArgumentsSource(ApplicationDocumentArgs.class)
        void shouldSendNotificationForNewUpload(UserDetails userDetails,
                                                DocumentUploaderType uploadedType,
                                                String uploadedBy,
                                                Set<DocumentUploadNotificationUserType> notificationTypes,
                                                List<String> expectedDocumentNames,
                                                boolean confidential) {
            verifyNotificationFurtherDocumentsTemplate(
                userDetails, uploadedType, EMPTY_CASE_DATA_MODIFIER,
                (caseData) ->  caseData.getApplicationDocuments().addAll(
                    wrapElements(createDummyApplicationDocument(NON_CONFIDENTIAL_1, uploadedBy,
                        PDF_DOCUMENT_1, confidential))),
                notificationTypes, expectedDocumentNames);
        }

        @ParameterizedTest
        @ArgumentsSource(ApplicationDocumentArgs.class)
        void shouldSendNotificationForUpdatingWhenReplacingDocument(
            UserDetails userDetails,
            DocumentUploaderType uploadedType,
            String uploadedBy,
            Set<DocumentUploadNotificationUserType> notificationTypes,
            List<String> expectedDocumentNames,
            boolean confidential) {
            verifyNotificationFurtherDocumentsTemplate(
                userDetails, uploadedType,
                (caseData) ->  caseData.getApplicationDocuments().addAll(
                    wrapElements(createDummyApplicationDocument(NON_CONFIDENTIAL_1, uploadedBy,
                        PDF_DOCUMENT_2, confidential))),
                (caseData) ->  caseData.getApplicationDocuments().addAll(
                    wrapElements(createDummyApplicationDocument(NON_CONFIDENTIAL_1, uploadedBy,
                        PDF_DOCUMENT_1, confidential))),
                notificationTypes, expectedDocumentNames);
        }

        @ParameterizedTest
        @ArgumentsSource(ApplicationDocumentConfidentialChangeArgs.class)
        void shouldNotSendNotificationForConfidentialChangeOnly(UserDetails userDetails,
                                                                DocumentUploaderType uploadedType,
                                                                String uploadedBy,
                                                                boolean oldConfidential,
                                                                boolean newConfidential) {
            verifyNotificationFurtherDocumentsTemplate(
                userDetails, uploadedType,
                (caseData) ->  caseData.getApplicationDocuments().addAll(
                    wrapElements(createDummyApplicationDocument(NON_CONFIDENTIAL_1, uploadedBy,
                        PDF_DOCUMENT_1, oldConfidential))),
                (caseData) ->  caseData.getApplicationDocuments().addAll(
                    wrapElements(createDummyApplicationDocument(NON_CONFIDENTIAL_1, uploadedBy,
                        PDF_DOCUMENT_1, newConfidential))),
                Set.of(), null);
        }
    }

    static class AnyOtherDocumentUploadArgs implements ArgumentsProvider {

        static final boolean HEARING_RELATED_YES = true;
        static final boolean HEARING_RELATED_NO = false;

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                // By DESIGNATED_LOCAL_AUTHORITY
                of(DESIGNATED_LOCAL_AUTHORITY,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR, CAFCASS),
                    NON_CONFIDENTIAL,
                    buildNonConfidentialPdfDocumentList(LA_USER),
                    HEARING_RELATED_NO),
                of(DESIGNATED_LOCAL_AUTHORITY,
                    Set.of(ALL_LAS),
                    CONFIDENTIAL,
                    buildConfidentialDocumentList(LA_USER),
                    HEARING_RELATED_NO),
                of(DESIGNATED_LOCAL_AUTHORITY,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR, CAFCASS),
                    NON_CONFIDENTIAL,
                    buildHearingFurtherEvidenceBundle(buildNonConfidentialPdfDocumentList(LA_USER)),
                    HEARING_RELATED_YES),
                of(DESIGNATED_LOCAL_AUTHORITY,
                    Set.of(ALL_LAS),
                    CONFIDENTIAL,
                    buildHearingFurtherEvidenceBundle(buildConfidentialDocumentList(LA_USER)),
                    HEARING_RELATED_YES),
                // Not notifying CAFCASS if document type is NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE
                of(DESIGNATED_LOCAL_AUTHORITY,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR),
                    NON_CONFIDENTIAL,
                    buildNonConfidentialPdfDocumentList(LA_USER, NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE),
                    HEARING_RELATED_NO),
                of(DESIGNATED_LOCAL_AUTHORITY,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR),
                    NON_CONFIDENTIAL,
                    buildHearingFurtherEvidenceBundle(buildNonConfidentialPdfDocumentList(LA_USER,
                        NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE)),
                    HEARING_RELATED_YES),
                of(DESIGNATED_LOCAL_AUTHORITY,
                    Set.of(ALL_LAS),
                    CONFIDENTIAL,
                    buildConfidentialDocumentList(LA_USER, NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE),
                    HEARING_RELATED_NO),
                of(DESIGNATED_LOCAL_AUTHORITY,
                    Set.of(ALL_LAS),
                    CONFIDENTIAL,
                    buildHearingFurtherEvidenceBundle(buildConfidentialDocumentList(LA_USER,
                        NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE)),
                    HEARING_RELATED_YES),

                // By HMCTS
                of(HMCTS,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR, CAFCASS),
                    NON_CONFIDENTIAL,
                    buildNonConfidentialPdfDocumentList(HMCTS_USER),
                    HEARING_RELATED_NO),
                of(HMCTS,
                    Set.of(),
                    CONFIDENTIAL,
                    buildConfidentialDocumentList(HMCTS_USER),
                    HEARING_RELATED_NO),
                of(HMCTS,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR, CAFCASS),
                    NON_CONFIDENTIAL,
                    buildHearingFurtherEvidenceBundle(buildNonConfidentialPdfDocumentList(HMCTS_USER)),
                    HEARING_RELATED_YES),
                of(HMCTS,
                    Set.of(),
                    CONFIDENTIAL,
                    buildHearingFurtherEvidenceBundle(buildConfidentialDocumentList(HMCTS_USER)),
                    HEARING_RELATED_YES),
                // Not notifying CAFCASS if document type is NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE
                of(HMCTS,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR),
                    NON_CONFIDENTIAL,
                    buildNonConfidentialPdfDocumentList(HMCTS_USER, NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE),
                    HEARING_RELATED_NO),
                of(HMCTS,
                    Set.of(),
                    CONFIDENTIAL,
                    buildConfidentialDocumentList(HMCTS_USER, NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE),
                    HEARING_RELATED_NO),
                of(HMCTS,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR),
                    NON_CONFIDENTIAL,
                    buildHearingFurtherEvidenceBundle(buildNonConfidentialPdfDocumentList(HMCTS_USER,
                        NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE)),
                    HEARING_RELATED_YES),
                of(HMCTS,
                    Set.of(),
                    CONFIDENTIAL,
                    buildHearingFurtherEvidenceBundle(buildConfidentialDocumentList(HMCTS_USER,
                        NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE)),
                    HEARING_RELATED_YES),
                // By Solicitor - no confidential document for solicitors
                of(SOLICITOR,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR, CAFCASS),
                    NON_CONFIDENTIAL,
                    buildNonConfidentialPdfDocumentList(REP_USER),
                    HEARING_RELATED_NO),
                of(SOLICITOR,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR, CAFCASS),
                    NON_CONFIDENTIAL,
                    buildHearingFurtherEvidenceBundle(buildNonConfidentialPdfDocumentList(REP_USER)),
                    HEARING_RELATED_YES),
                // Not notifying CAFCASS if document type is NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE
                of(SOLICITOR,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR),
                    NON_CONFIDENTIAL,
                    buildNonConfidentialPdfDocumentList(REP_USER, NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE),
                    HEARING_RELATED_NO),
                of(SOLICITOR,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR),
                    NON_CONFIDENTIAL,
                    buildHearingFurtherEvidenceBundle(buildNonConfidentialPdfDocumentList(REP_USER,
                        NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE)),
                    HEARING_RELATED_YES)
            );
        }
    }

    @Nested
    class AnyOtherDocument {

        private List<Element<SupportingEvidenceBundle>> document(CaseData caseData,
                                                                 DocumentUploaderType uploaderType) {
            switch (uploaderType) {
                case DESIGNATED_LOCAL_AUTHORITY:
                    return caseData.getFurtherEvidenceDocumentsLA();
                case HMCTS:
                    return caseData.getFurtherEvidenceDocuments();
                case SOLICITOR:
                    return caseData.getFurtherEvidenceDocumentsSolicitor();
                default:
                    throw new AssertionError("unexpected uploaderType");
            }
        }

        @ParameterizedTest
        @ArgumentsSource(AnyOtherDocumentUploadArgs.class)
        @SuppressWarnings("unchecked")
        void shouldSendNotificationForNewUpload(DocumentUploaderType uploaderType,
            Set<DocumentUploadNotificationUserType> notificationTypes,
            List<String> expectedDocumentNames,
            List<?> updatingDocument,
            boolean isRelatingToHearing) {
            verifyNotificationFurtherDocumentsTemplate(
                getUserDetails(uploaderType), uploaderType,
                EMPTY_CASE_DATA_MODIFIER,
                isRelatingToHearing
                    ? (caseData) -> caseData.getHearingFurtherEvidenceDocuments()
                    .addAll((List<Element<HearingFurtherEvidenceBundle>>) updatingDocument)
                    : (caseData) -> document(caseData, uploaderType)
                    .addAll((List<Element<SupportingEvidenceBundle>>) updatingDocument),
                notificationTypes, expectedDocumentNames);
        }

        @ParameterizedTest
        @ArgumentsSource(AnyOtherDocumentUploadArgs.class)
        @SuppressWarnings("unchecked")
        void shouldNotSendNotificationWhenDocsAreRemoved(DocumentUploaderType uploaderType,
                                                         Set<DocumentUploadNotificationUserType> notificationTypes,
                                                         List<String> expectedDocumentNames,
                                                         List<?> originalDocuments,
                                                         boolean isRelatingToHearing) {
            verifyNotificationFurtherDocumentsTemplate(
                getUserDetails(uploaderType), uploaderType,
                isRelatingToHearing
                    ? (caseData) -> caseData.getHearingFurtherEvidenceDocuments()
                    .addAll((List<Element<HearingFurtherEvidenceBundle>>) originalDocuments)
                    : (caseData) -> document(caseData, uploaderType)
                    .addAll((List<Element<SupportingEvidenceBundle>>) originalDocuments),
                EMPTY_CASE_DATA_MODIFIER,
                Set.of(), null);
        }

        @ParameterizedTest
        @ArgumentsSource(AnyOtherDocumentUploadArgs.class)
        @SuppressWarnings("unchecked")
        void shouldNotSendNotificationWhenDocsAreTheSame(DocumentUploaderType uploaderType,
                                                Set<DocumentUploadNotificationUserType> notificationTypes,
                                                List<String> expectedDocumentNames,
                                                List<?> updatingDocument,
                                                boolean isRelatingToHearing) {
            verifyNotificationFurtherDocumentsTemplate(
                getUserDetails(uploaderType), uploaderType,
                isRelatingToHearing
                    ? (caseData) -> caseData.getHearingFurtherEvidenceDocuments()
                    .addAll((List<Element<HearingFurtherEvidenceBundle>>) updatingDocument)
                    : (caseData) -> document(caseData, uploaderType)
                    .addAll((List<Element<SupportingEvidenceBundle>>) updatingDocument),
                isRelatingToHearing
                    ? (caseData) -> caseData.getHearingFurtherEvidenceDocuments()
                    .addAll((List<Element<HearingFurtherEvidenceBundle>>) updatingDocument)
                    : (caseData) -> document(caseData, uploaderType)
                    .addAll((List<Element<SupportingEvidenceBundle>>) updatingDocument),
                Set.of(), null);
        }
    }
}
