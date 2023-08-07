package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
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
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.events.FurtherEvidenceUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseSummary;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingDocuments;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.PositionStatementChild;
import uk.gov.hmcts.reform.fpl.model.PositionStatementRespondent;
import uk.gov.hmcts.reform.fpl.model.RespondentStatement;
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
import uk.gov.hmcts.reform.fpl.service.FurtherEvidenceNotificationService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.furtherevidence.FurtherEvidenceUploadDifferenceCalculator;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;
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
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.BIRTH_CERTIFICATE;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.ADJOURNED;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance.IN_PERSON;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType.ALL_LAS;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType.CAFCASS_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType.CHILD_SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploadNotificationUserType.RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.HMCTS;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.CONFIDENTIAL_1;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.CONFIDENTIAL_2;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.LA_USER;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.NON_CONFIDENTIAL_1;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.NON_CONFIDENTIAL_2;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.PDF_DOCUMENT_1;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.PDF_DOCUMENT_2;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.PDF_DOCUMENT_3;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.REP_SOLICITOR_USER_EMAIL;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithAdditionalApplicationBundle;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithC2AdditionalApplicationBundle;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithConfidentialLADocuments;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildCaseDataWithNonConfidentialPDFRespondentStatementsSolicitor;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildConfidentialDocumentList;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildHearingFurtherEvidenceBundle;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildNonConfidentialDocumentList;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildRespondentStatementsList;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.buildSubmittedCaseData;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.createCourtBundleList;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.createDummyApplicationDocument;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.createDummyEvidenceBundle;
import static uk.gov.hmcts.reform.fpl.handlers.FurtherEvidenceUploadedEventTestData.removeEvidenceBundleType;
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
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeCollection;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithUUIDs;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FurtherEvidenceUploadedEventHandlerTest {
    private static final String HMCTS_USER = "HMCTS";
    private static final String REP_USER = "REP";
    private static final String LA_USER_EMAIL = "la@examaple.com";
    private static final String LA2_USER_EMAIL = "la2@examaple.com";
    private static final String CAFCASS_REPRESENTATIVE_EMAIL = "cafcass@examaple.com";
    private static final String SENDER_FORENAME = "The";
    private static final String SENDER_SURNAME = "Sender";
    private static final String SENDER = SENDER_FORENAME + " " + SENDER_SURNAME;
    private static final String REP_SOLICITOR_1_EMAIL = "rep_solicitor1@example.com";
    private static final String REP_SOLICITOR_2_EMAIL = "rep_solicitor2@example.com";
    private static final String REP_SOLICITOR_3_EMAIL = "rep_solicitor3@example.com";
    private static final String REP_SOLICITOR_4_EMAIL = "rep_solicitor4@example.com";
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

    private static final Consumer<CaseData> EMPTY_CASE_DATA_MODIFIER = whatever -> { };

    private void verifyCafcassNotificationTemplate(
        DocumentUploaderType uploaderType,
        Consumer<CaseData> beforeCaseDataModifier,
        Consumer<CaseData> caseDataModifier,

        Function<CaseData, Set<DocumentReference>> documentReferencesExtractor,
        String documentTypeString, String emailSubjectInfo) {

        CaseData caseDataBefore = buildSubmittedCaseData();
        beforeCaseDataModifier.accept(caseDataBefore);
        CaseData caseData = buildSubmittedCaseData();
        caseDataModifier.accept(caseData);

        when(cafcassLookupConfiguration.getCafcassEngland(any())).thenReturn(
            Optional.of(new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)));

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(caseData, caseDataBefore, uploaderType,
                getUserDetails(uploaderType));
        furtherEvidenceUploadedEventHandler.sendDocumentsToCafcass(furtherEvidenceUploadedEvent);

        if (isEmpty(documentTypeString)) {
            verify(cafcassNotificationService, never()).sendEmail(any(), any(), any(), any());
        } else {
            verify(cafcassNotificationService).sendEmail(
                eq(caseData),
                eq(documentReferencesExtractor.apply(caseData)),
                eq(NEW_DOCUMENT),
                newDocumentDataCaptor.capture());

            NewDocumentData newDocumentData = newDocumentDataCaptor.getValue();
            assertThat(newDocumentData.getDocumentTypes())
                .isEqualTo(documentTypeString);
            assertThat(newDocumentData.getEmailSubjectInfo())
                .isEqualTo(emailSubjectInfo);
        }
    }

    private void verifyNotificationTemplate(DocumentUploaderType uploaderType,
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
        final Set<String> allCafcassRepresentativeEmails = Set.of(CAFCASS_REPRESENTATIVE_EMAIL);
        when(furtherEvidenceNotificationService.getCafcassRepresentativeEmails(caseData))
            .thenReturn(Set.of(CAFCASS_REPRESENTATIVE_EMAIL));

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(caseData, caseDataBefore, uploaderType,
                getUserDetails(uploaderType));
        furtherEvidenceUploadedEventHandler.sendDocumentsUploadedNotification(furtherEvidenceUploadedEvent);

        if (!notificationTypes.isEmpty()) {
            if (notificationTypes.contains(ALL_LAS)) {
                verify(furtherEvidenceNotificationService).sendNotification(
                    any(), eq(allLAs), eq(SENDER), eq(expectedDocumentNames));
            } else {
                verify(furtherEvidenceNotificationService, never()).sendNotification(any(),
                    eq(allLAs), any(), any());
            }
            if (notificationTypes.contains(CAFCASS_REPRESENTATIVES)) {
                verify(furtherEvidenceNotificationService).sendNotification(
                    any(), eq(allCafcassRepresentativeEmails), eq(SENDER), eq(expectedDocumentNames));
            } else {
                verify(furtherEvidenceNotificationService, never()).sendNotification(any(),
                    eq(allCafcassRepresentativeEmails), any(), any());
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

    private static boolean isNotifyingCafcass(Set<DocumentUploadNotificationUserType> notificationUserTypes) {
        return nullSafeCollection(notificationUserTypes).contains(CAFCASS_REPRESENTATIVES);
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

    // Cafcass Notification: Documents for additional applications
    @Test
    void shouldNotEmailCafcassWhenConfidentialAdditionalBundleIsUploadedByHmcts() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
            .thenReturn(
                Optional.of(
                    new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                )
            );

        CaseData beforeCaseData = buildSubmittedCaseData();
        CaseData caseData = buildCaseDataWithAdditionalApplicationBundle(HMCTS_USER, true);

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                beforeCaseData,
                HMCTS,
                userDetailsHMCTS());

        furtherEvidenceUploadedEventHandler.sendDocumentsToCafcass(furtherEvidenceUploadedEvent);

        Set<DocumentReference> documentReferences = unwrapElements(caseData.getAdditionalApplicationsBundle())
            .stream()
            .map(AdditionalApplicationsBundle::getOtherApplicationsBundle)
            .map(OtherApplicationsBundle::getAllDocumentReferences)
            .flatMap(List::stream)
            .map(Element::getValue)
            .collect(toSet());

        verify(cafcassNotificationService, never()).sendEmail(
            eq(caseData),
            eq(documentReferences),
            eq(NEW_DOCUMENT),
            newDocumentDataCaptor.capture());
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
    void shouldEmailCafcassWhenConfidentialC2AdditionalBundleIsUploadedByHmcts() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
            .thenReturn(
                Optional.of(
                    new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)
                )
            );

        CaseData beforeCaseData = buildSubmittedCaseData();
        CaseData caseData = buildCaseDataWithC2AdditionalApplicationBundle(HMCTS_USER, true);

        FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
            new FurtherEvidenceUploadedEvent(
                caseData,
                beforeCaseData,
                HMCTS,
                userDetailsHMCTS());

        furtherEvidenceUploadedEventHandler.sendDocumentsToCafcass(furtherEvidenceUploadedEvent);

        Set<DocumentReference> documentReferences = unwrapElements(caseData.getAdditionalApplicationsBundle())
            .stream()
            .map(AdditionalApplicationsBundle::getC2DocumentBundle)
            .map(C2DocumentBundle::getAllC2DocumentReferences)
            .flatMap(List::stream)
            .map(Element::getValue)
            .collect(toSet());

        verify(cafcassNotificationService, never()).sendEmail(
            eq(caseData),
            eq(documentReferences),
            eq(NEW_DOCUMENT),
            newDocumentDataCaptor.capture());
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

    static class CourtBundleUploadTestsArgs implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                of(DESIGNATED_LOCAL_AUTHORITY, true),
                of(DESIGNATED_LOCAL_AUTHORITY, false),
                of(HMCTS, true),
                of(HMCTS, false)
            );
        }
    }

    @Nested
    class CourtBundleUploadTests {

        private void verifyCafcassNotificationForCourtBundleTemplate(DocumentUploaderType uploaderType,
                                                                     Consumer<CaseData> beforeCaseDataModifier,
                                                                     Consumer<CaseData> caseDataModifier,
                                                                     List<List<Element<HearingCourtBundle>>>
                                                                         expectedHearingCourtBundles,
                                                                     List<List<Element<HearingCourtBundle>>>
                                                                         unexpectedHearingCourtBundles) {
            CaseData caseDataBefore = buildSubmittedCaseData();
            beforeCaseDataModifier.accept(caseDataBefore);
            CaseData caseData = buildSubmittedCaseData();
            caseDataModifier.accept(caseData);

            when(cafcassLookupConfiguration.getCafcassEngland(any())).thenReturn(
                Optional.of(new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)));

            FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(caseData, caseDataBefore, uploaderType,
                    getUserDetails(uploaderType));

            furtherEvidenceUploadedEventHandler.sendCourtBundlesToCafcass(furtherEvidenceUploadedEvent);

            for (List<Element<HearingCourtBundle>> bundle : expectedHearingCourtBundles) {
                Set<DocumentReference> documentReferences = bundle.stream()
                    .map(hearingCourtBundle -> unwrapElements(hearingCourtBundle.getValue().getCourtBundle()))
                    .flatMap(List::stream)
                    .map(courtBundle -> courtBundle.getDocument())
                    .collect(toSet());

                verify(cafcassNotificationService).sendEmail(eq(caseData),
                    eq(documentReferences),
                    eq(COURT_BUNDLE),
                    courtBundleCaptor.capture());

                CourtBundleData courtBundleData = courtBundleCaptor.getValue();
                assertThat(courtBundleData.getHearingDetails()).isEqualTo(bundle.get(0).getValue().getHearing());
            }
            for (List<Element<HearingCourtBundle>> bundle : unexpectedHearingCourtBundles) {
                Set<DocumentReference> documentReferences = bundle.stream()
                    .map(hearingCourtBundle -> unwrapElements(hearingCourtBundle.getValue().getCourtBundle()))
                    .flatMap(List::stream)
                    .map(courtBundle -> courtBundle.getDocument())
                    .collect(toSet());

                verify(cafcassNotificationService, never()).sendEmail(eq(caseData), eq(documentReferences),
                    eq(COURT_BUNDLE), any());
            }
        }

        private void verifyNotificationForCourtBundleTemplate(DocumentUploaderType uploaderType,
                                                              Consumer<CaseData> beforeCaseDataModifier,
                                                              Consumer<CaseData> caseDataModifier,
                                                              List<String> expectedHearingDetails,
                                                              List<String> unexpectedHearingDetails) {
            CaseData caseDataBefore = buildSubmittedCaseData();
            beforeCaseDataModifier.accept(caseDataBefore);
            CaseData caseData = buildSubmittedCaseData();
            caseDataModifier.accept(caseData);

            when(furtherEvidenceNotificationService.getChildSolicitorEmails(caseData))
                .thenReturn(Set.of(REP_SOLICITOR_1_EMAIL));
            when(furtherEvidenceNotificationService.getRespondentSolicitorEmails(caseData))
                .thenReturn(Set.of(REP_SOLICITOR_2_EMAIL));
            when(furtherEvidenceNotificationService.getDesignatedLocalAuthorityRecipients(caseData))
                .thenReturn(Set.of(LA_USER_EMAIL));
            when(furtherEvidenceNotificationService.getLocalAuthoritiesRecipients(caseData))
                .thenReturn(Set.of(LA2_USER_EMAIL));

            FurtherEvidenceUploadedEvent furtherEvidenceUploadedEvent =
                new FurtherEvidenceUploadedEvent(
                    caseData,
                    caseDataBefore,
                    uploaderType,
                    getUserDetails(uploaderType));

            furtherEvidenceUploadedEventHandler.sendCourtBundlesUploadedNotification(furtherEvidenceUploadedEvent);

            for (String hearingDetail : expectedHearingDetails) {
                verify(furtherEvidenceNotificationService).sendNotificationForCourtBundleUploaded(caseData,
                    Set.of(REP_SOLICITOR_1_EMAIL, REP_SOLICITOR_2_EMAIL, LA_USER_EMAIL, LA2_USER_EMAIL),
                    hearingDetail);
            }
            for (String unexpectedHearingDetail : unexpectedHearingDetails) {
                verify(furtherEvidenceNotificationService, never()).sendNotificationForCourtBundleUploaded(
                    any(),
                    any(),
                    eq(unexpectedHearingDetail));
            }
        }

        @ParameterizedTest
        @ArgumentsSource(CourtBundleUploadTestsArgs.class)
        void shouldSendNotificationWhenNewCourtBundlesAreUploaded(DocumentUploaderType uploaderType,
                                                                  boolean confidential) {
            String hearing1 = "1stHearing";
            String hearing2 = "2ndHearing";
            String hearing3 = "3rdHearing";
            String uploadedBy = DESIGNATED_LOCAL_AUTHORITY.equals(uploaderType) ? LA_USER : HMCTS_USER;

            final List<Element<HearingCourtBundle>> firstHearingBundle = createCourtBundleList(2, hearing1, uploadedBy,
                confidential);
            final List<Element<HearingCourtBundle>> secondHearingBundle = createCourtBundleList(2, hearing2, uploadedBy,
                confidential);
            final List<Element<HearingCourtBundle>> thirdHearingBundle = createCourtBundleList(2, hearing3, uploadedBy,
                confidential);

            List<Element<HearingCourtBundle>> totalHearing = new ArrayList<>();
            totalHearing.addAll(firstHearingBundle);
            totalHearing.addAll(secondHearingBundle);
            totalHearing.addAll(thirdHearingBundle);
            Collections.shuffle(totalHearing);

            verifyNotificationForCourtBundleTemplate(uploaderType,
                EMPTY_CASE_DATA_MODIFIER,
                (caseData) -> caseData.getHearingDocuments().getCourtBundleListV2().addAll(totalHearing),
                HMCTS.equals(uploaderType) && confidential ? List.of() : List.of(hearing1, hearing2, hearing3),
                HMCTS.equals(uploaderType) && confidential ? List.of(hearing1, hearing2, hearing3) : List.of());
            verifyCafcassNotificationForCourtBundleTemplate(uploaderType,
                EMPTY_CASE_DATA_MODIFIER,
                (caseData) -> caseData.getHearingDocuments().getCourtBundleListV2().addAll(totalHearing),
                HMCTS.equals(uploaderType) && confidential
                    ? List.of() : List.of(firstHearingBundle, secondHearingBundle, thirdHearingBundle),
                HMCTS.equals(uploaderType) && confidential
                    ? List.of(firstHearingBundle, secondHearingBundle, thirdHearingBundle) : List.of());
        }

        @ParameterizedTest
        @ArgumentsSource(CourtBundleUploadTestsArgs.class)
        void shouldSendNotificationWhenUpdatingCourtBundles(DocumentUploaderType uploaderType,
                                                            boolean confidential) {
            String hearing1 = "1stHearing";
            String hearing2 = "2ndHearing";
            String hearing3 = "3rdHearing";
            String uploadedBy = DESIGNATED_LOCAL_AUTHORITY.equals(uploaderType) ? LA_USER : HMCTS_USER;

            final List<Element<HearingCourtBundle>> firstHearingBundle = createCourtBundleList(2, hearing1, uploadedBy,
                confidential);
            final List<Element<HearingCourtBundle>> secondHearingBundle = createCourtBundleList(2, hearing2, uploadedBy,
                confidential);
            final List<Element<HearingCourtBundle>> thirdHearingBundle = createCourtBundleList(2, hearing3, uploadedBy,
                confidential);
            final List<Element<HearingCourtBundle>> updatedFirstHearingBundle = createCourtBundleList(2, hearing1,
                uploadedBy, confidential);

            List<Element<HearingCourtBundle>> beforeHearings = new ArrayList<>();
            beforeHearings.addAll(firstHearingBundle);
            beforeHearings.addAll(secondHearingBundle);
            beforeHearings.addAll(thirdHearingBundle);
            Collections.shuffle(beforeHearings);

            List<Element<HearingCourtBundle>> afterHearings = new ArrayList<>();
            afterHearings.addAll(updatedFirstHearingBundle);
            afterHearings.addAll(secondHearingBundle);
            afterHearings.addAll(thirdHearingBundle);
            Collections.shuffle(afterHearings);

            verifyNotificationForCourtBundleTemplate(uploaderType,
                (caseData) -> caseData.getHearingDocuments().getCourtBundleListV2().addAll(beforeHearings),
                (caseData) -> caseData.getHearingDocuments().getCourtBundleListV2().addAll(afterHearings),
                HMCTS.equals(uploaderType) && confidential ? List.of() : List.of(hearing1),
                HMCTS.equals(uploaderType) && confidential
                    ? List.of(hearing1, hearing2, hearing3) : List.of(hearing2, hearing3));
            verifyCafcassNotificationForCourtBundleTemplate(uploaderType,
                (caseData) -> caseData.getHearingDocuments().getCourtBundleListV2().addAll(beforeHearings),
                (caseData) -> caseData.getHearingDocuments().getCourtBundleListV2().addAll(afterHearings),
                HMCTS.equals(uploaderType) && confidential ? List.of() : List.of(updatedFirstHearingBundle),
                HMCTS.equals(uploaderType) && confidential
                    ? List.of(updatedFirstHearingBundle, secondHearingBundle, thirdHearingBundle)
                    : List.of(secondHearingBundle, thirdHearingBundle));
        }

        @ParameterizedTest
        @ArgumentsSource(CourtBundleUploadTestsArgs.class)
        void shouldNotSendNotificationWhenNoNewCourtBundleIsUploaded(DocumentUploaderType uploaderType,
                                                                     boolean confidential) {
            String hearing1 = "1stHearing";
            String hearing2 = "2ndHearing";
            String hearing3 = "3rdHearing";
            String uploadedBy = DESIGNATED_LOCAL_AUTHORITY.equals(uploaderType) ? LA_USER : HMCTS_USER;
            List<Element<HearingCourtBundle>> firstHearingBundle = createCourtBundleList(2, hearing1, uploadedBy,
                confidential);
            List<Element<HearingCourtBundle>> secondHearingBundle = createCourtBundleList(2, hearing2, uploadedBy,
                confidential);
            List<Element<HearingCourtBundle>> thirdHearingBundle = createCourtBundleList(2, hearing3, uploadedBy,
                confidential);

            List<Element<HearingCourtBundle>> totalHearing = new ArrayList<>();
            totalHearing.addAll(firstHearingBundle);
            totalHearing.addAll(secondHearingBundle);
            totalHearing.addAll(thirdHearingBundle);
            Collections.shuffle(totalHearing);

            verifyNotificationForCourtBundleTemplate(uploaderType,
                (caseData) -> caseData.getHearingDocuments().getCourtBundleListV2().addAll(totalHearing),
                (caseData) -> caseData.getHearingDocuments().getCourtBundleListV2().addAll(totalHearing),
                emptyList(), List.of(hearing1, hearing2, hearing3));
            verifyCafcassNotificationForCourtBundleTemplate(uploaderType,
                (caseData) -> caseData.getHearingDocuments().getCourtBundleListV2().addAll(totalHearing),
                (caseData) -> caseData.getHearingDocuments().getCourtBundleListV2().addAll(totalHearing),
                List.of(), List.of(firstHearingBundle, secondHearingBundle, thirdHearingBundle));
        }

        @ParameterizedTest
        @ArgumentsSource(CourtBundleUploadTestsArgs.class)
        void shouldNotSendNotificationWhenNoCourtBundleIsUploadedByLA(DocumentUploaderType uploaderType,
                                                                      boolean confidential) {
            verifyNotificationForCourtBundleTemplate(
                uploaderType,
                EMPTY_CASE_DATA_MODIFIER,
                EMPTY_CASE_DATA_MODIFIER,
                emptyList(), List.of());
            verifyCafcassNotificationForCourtBundleTemplate(
                uploaderType,
                EMPTY_CASE_DATA_MODIFIER,
                EMPTY_CASE_DATA_MODIFIER,
                emptyList(), List.of());
        }
    }

    // Hearing Document > Case Summary/Position Statement/Skeleton argument
    @Test
    void shouldSendNotificationWhenHearingDocumentsIsUploaded() {
        final List<Element<HearingBooking>> hearingBooking = wrapElementsWithUUIDs(testHearing());
        final String hearingBookingLabel = hearingBooking.get(0).getValue().toLabel();
        final List<Element<CaseSummary>> caseSummaryList = wrapElementsWithUUIDs(
            CaseSummary.builder().hearing(hearingBookingLabel)
                .document(TestDataHelper.testDocumentReference("CaseSummary 1.pdf")).build(),
            CaseSummary.builder().hearing(hearingBookingLabel)
                .document(TestDataHelper.testDocumentReference("CaseSummary 2.pdf")).build());
        final List<Element<PositionStatementChild>> positionStatementChildList = wrapElementsWithUUIDs(
            PositionStatementChild.builder()
                .hearing(hearingBookingLabel)
                .document(TestDataHelper.testDocumentReference("PositionStatementChild.pdf")).build());
        final List<Element<PositionStatementRespondent>> positionStatementRespondentList = wrapElementsWithUUIDs(
            PositionStatementRespondent.builder()
                .hearing(hearingBookingLabel)
                .document(TestDataHelper.testDocumentReference("PositionStatementRespondent.pdf")).build());
        final List<Element<SkeletonArgument>> skeletonArgumentList = wrapElementsWithUUIDs(
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

        final List<Element<CaseSummary>> caseSummaryList = wrapElementsWithUUIDs(
            CaseSummary.builder().hearing(hearing).document(caseSummaryDoc1).build(),
            CaseSummary.builder().hearing(hearing).document(caseSummaryDoc2).build());
        final List<Element<PositionStatementChild>> positionStatementChildList = wrapElementsWithUUIDs(
            PositionStatementChild.builder().hearing(hearing).document(positionStatementChildDoc).build());
        final List<Element<PositionStatementRespondent>> positionStatementRespondentList = wrapElementsWithUUIDs(
            PositionStatementRespondent.builder().hearing(hearing).document(positionStatementRespondentDoc).build());
        final List<Element<SkeletonArgument>> skeletonArgumentList = wrapElementsWithUUIDs(
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

        final List<Element<CaseSummary>> caseSummaryList = wrapElementsWithUUIDs(
            CaseSummary.builder().hearing(hearing).document(caseSummaryDoc1).build(),
            CaseSummary.builder().hearing(hearing).document(caseSummaryDoc2).build());
        final List<Element<PositionStatementChild>> positionStatementChildList = wrapElementsWithUUIDs(
            PositionStatementChild.builder().hearing(hearing).document(positionStatementChildDoc).build());
        final List<Element<PositionStatementRespondent>> positionStatementRespondentList = wrapElementsWithUUIDs(
            PositionStatementRespondent.builder().hearing(hearing).document(positionStatementRespondentDoc).build());
        final List<Element<SkeletonArgument>> skeletonArgumentList = wrapElementsWithUUIDs(
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

    static class CorrespondenceUploadTestArgs implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                of(DESIGNATED_LOCAL_AUTHORITY, LA_USER, false),
                of(DESIGNATED_LOCAL_AUTHORITY, LA_USER, true),
                of(HMCTS, HMCTS_USER, false),
                of(HMCTS, HMCTS_USER, true),
                of(SOLICITOR, REP_SOLICITOR_USER_EMAIL, false)

            // Note: Respondent/Child Solicitor cannot upload confidential document
            );
        }
    }

    static class CorrespondenceConfidentialChangeArgs implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                of(DESIGNATED_LOCAL_AUTHORITY, LA_USER, true, false),
                of(DESIGNATED_LOCAL_AUTHORITY, LA_USER, false, true),
                of(HMCTS, HMCTS_USER, true, false),
                of(HMCTS, HMCTS_USER, false, true)
            );
        }
    }

    @Nested
    class CorrespondenceUploadTests {

        private Function<CaseData, Set<DocumentReference>> toDocumentReferencesExtractor(
            List<Element<SupportingEvidenceBundle>> correspondences) {
            return caseData -> unwrapElements(correspondences).stream().map(f -> f.getDocument()).collect(toSet());
        }

        private List<Element<SupportingEvidenceBundle>> getCorrespondenceDocuments(CaseData caseData,
                                                                                   DocumentUploaderType uploaderType) {
            switch (uploaderType) {
                case DESIGNATED_LOCAL_AUTHORITY:
                    return caseData.getCorrespondenceDocumentsLA();
                case HMCTS:
                    return caseData.getCorrespondenceDocuments();
                case SOLICITOR:
                    return caseData.getCorrespondenceDocumentsSolicitor();
                default:
                    return null;
            }
        }

        @ParameterizedTest
        @ArgumentsSource(CorrespondenceUploadTestArgs.class)
        void shouldSendNotificationForNewUpload(DocumentUploaderType uploaderType,
                                                String uploadedBy,
                                                boolean confidential) {
            List<Element<SupportingEvidenceBundle>> correspondences =
                removeEvidenceBundleType(confidential
                    ? buildConfidentialDocumentList(uploadedBy)
                    : buildNonConfidentialDocumentList(uploadedBy));
            verifyCafcassNotificationTemplate(
                uploaderType,
                EMPTY_CASE_DATA_MODIFIER,
                (caseData) ->  getCorrespondenceDocuments(caseData, uploaderType).addAll(correspondences),
                toDocumentReferencesExtractor(correspondences),
                HMCTS.equals(uploaderType) && confidential ? null : "• Correspondence",
                HMCTS.equals(uploaderType) && confidential ? null : "Correspondence");
        }

        @ParameterizedTest
        @ArgumentsSource(CorrespondenceUploadTestArgs.class)
        void shouldSendNotificationWhenDocsAreReplaced(DocumentUploaderType uploaderType,
                                                       String uploadedBy,
                                                       boolean confidential) {
            List<Element<SupportingEvidenceBundle>> beforeCorrespondences =
                removeEvidenceBundleType(confidential
                    ? buildConfidentialDocumentList(uploadedBy)
                    : buildNonConfidentialDocumentList(uploadedBy));
            List<Element<SupportingEvidenceBundle>> afterCorrespondences = beforeCorrespondences
                .stream()
                .map(seb -> element(seb.getId(), seb.getValue().toBuilder()
                    .document(PDF_DOCUMENT_3)
                    .build()))
                .toList();

            verifyCafcassNotificationTemplate(uploaderType,
                (caseData) ->  getCorrespondenceDocuments(caseData, uploaderType).addAll(beforeCorrespondences),
                (caseData) ->  getCorrespondenceDocuments(caseData, uploaderType).addAll(afterCorrespondences),
                toDocumentReferencesExtractor(afterCorrespondences),
                HMCTS.equals(uploaderType) && confidential ? null : "• Correspondence",
                HMCTS.equals(uploaderType) && confidential ? null : "Correspondence");
        }

        @ParameterizedTest
        @ArgumentsSource(CorrespondenceUploadTestArgs.class)
        void shouldNotSendNotificationWhenDocsAreRemoved(DocumentUploaderType uploaderType,
                                                         String uploadedBy,
                                                         boolean confidential) {
            List<Element<SupportingEvidenceBundle>> correspondences =
                removeEvidenceBundleType(confidential
                    ? buildConfidentialDocumentList(uploadedBy)
                    : buildNonConfidentialDocumentList(uploadedBy));
            verifyCafcassNotificationTemplate(
                uploaderType,
                (caseData) ->  getCorrespondenceDocuments(caseData, uploaderType).addAll(correspondences),
                EMPTY_CASE_DATA_MODIFIER,
                toDocumentReferencesExtractor(correspondences),
                null, null);
        }

        @ParameterizedTest
        @ArgumentsSource(CorrespondenceUploadTestArgs.class)
        void shouldNotSendNotificationWhenDocsAreTheSame(DocumentUploaderType uploaderType,
                                                         String uploadedBy,
                                                         boolean confidential) {
            List<Element<SupportingEvidenceBundle>> correspondences =
                removeEvidenceBundleType(confidential
                    ? buildConfidentialDocumentList(uploadedBy)
                    : buildNonConfidentialDocumentList(uploadedBy));
            verifyCafcassNotificationTemplate(
                uploaderType,
                (caseData) ->  getCorrespondenceDocuments(caseData, uploaderType).addAll(correspondences),
                (caseData) ->  getCorrespondenceDocuments(caseData, uploaderType).addAll(correspondences),
                toDocumentReferencesExtractor(correspondences),
                null, null);
        }

        @ParameterizedTest
        @ArgumentsSource(CorrespondenceConfidentialChangeArgs.class)
        void shouldNotSendNotificationWhenConfidentialChanged(DocumentUploaderType uploaderType,
                                                              String uploadedBy,
                                                              boolean newConfidential,
                                                              boolean oldConfidential) {
            List<Element<SupportingEvidenceBundle>> beforeCorrespondences =
                removeEvidenceBundleType(oldConfidential
                    ? buildConfidentialDocumentList(uploadedBy)
                    : buildNonConfidentialDocumentList(uploadedBy));
            List<Element<SupportingEvidenceBundle>> afterCorrespondences =
                beforeCorrespondences.stream()
                    .map(seb -> element(seb.getId(), seb.getValue().toBuilder()
                        .confidential(newConfidential ? List.of("CONFIDENTIAL") : null)
                        .build()))
                    .toList();
            verifyCafcassNotificationTemplate(
                uploaderType,
                (caseData) ->  getCorrespondenceDocuments(caseData, uploaderType).addAll(beforeCorrespondences),
                (caseData) ->  getCorrespondenceDocuments(caseData, uploaderType).addAll(afterCorrespondences),
                toDocumentReferencesExtractor(afterCorrespondences),
                null, null);
        }
    }

    static class RespondentStatementConfidentialChangeArgs implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                of(DESIGNATED_LOCAL_AUTHORITY, LA_USER, true, false),
                of(DESIGNATED_LOCAL_AUTHORITY, LA_USER, false, true),
                of(HMCTS, HMCTS_USER, true, false),
                of(HMCTS, HMCTS_USER, false, true)
            );
        }
    }

    static class RespondentStatementUploadTestArgs implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                of(DESIGNATED_LOCAL_AUTHORITY, LA_USER,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR, CAFCASS_REPRESENTATIVES), NON_CONFIDENTIAL,
                    false),
                of(DESIGNATED_LOCAL_AUTHORITY, LA_USER,
                    Set.of(ALL_LAS, CAFCASS_REPRESENTATIVES), CONFIDENTIAL, true),
                of(HMCTS, HMCTS_USER,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR, CAFCASS_REPRESENTATIVES), NON_CONFIDENTIAL,
                    false),
                of(HMCTS, HMCTS_USER,
                    Set.of(), CONFIDENTIAL, true),
                of(SOLICITOR, REP_USER,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR, CAFCASS_REPRESENTATIVES), NON_CONFIDENTIAL,
                    false)
            // Note: Respondent/Child Solicitor cannot upload confidential document
            );
        }
    }

    @Nested
    class RespondentStatementUploadTests {

        private Function<CaseData, Set<DocumentReference>> toDocumentReferencesExtractor(
            List<Element<RespondentStatement>> respondentStatements) {
            return caseData -> unwrapElements(respondentStatements).stream()
                .map(rs -> unwrapElements(rs.getSupportingEvidenceBundle()))
                .flatMap(List::stream)
                .map(f -> f.getDocument())
                .collect(toSet());
        }

        @ParameterizedTest
        @ArgumentsSource(RespondentStatementUploadTestArgs.class)
        void shouldSendNotificationForNewUpload(DocumentUploaderType uploaderType,
                                                String uploadedBy,
                                                Set<DocumentUploadNotificationUserType> notificationTypes,
                                                List<String> expectedDocumentNames,
                                                boolean confidential) {
            List<Element<RespondentStatement>> respondentStatements =  buildRespondentStatementsList(
                confidential
                    ? buildConfidentialDocumentList(uploadedBy)
                    : buildNonConfidentialDocumentList(uploadedBy)
            );
            verifyNotificationTemplate(uploaderType, EMPTY_CASE_DATA_MODIFIER,
                (caseData) ->  caseData.getRespondentStatements().addAll(respondentStatements),
                notificationTypes, expectedDocumentNames);
            verifyCafcassNotificationTemplate(uploaderType,
                EMPTY_CASE_DATA_MODIFIER,
                (caseData) ->  caseData.getRespondentStatements().addAll(respondentStatements),
                toDocumentReferencesExtractor(respondentStatements),
                isNotifyingCafcass(notificationTypes) ? "• Respondent statement" : null,
                isNotifyingCafcass(notificationTypes) ? "Further documents for main application" : null);
        }

        @ParameterizedTest
        @ArgumentsSource(RespondentStatementUploadTestArgs.class)
        void shouldSendNotificationWhenDocsAreReplaced(DocumentUploaderType uploaderType,
                                                       String uploadedBy,
                                                       Set<DocumentUploadNotificationUserType> notificationTypes,
                                                       List<String> expectedDocumentNames,
                                                       boolean confidential) {
            List<Element<RespondentStatement>> respondentStatements =  buildRespondentStatementsList(
                confidential
                    ? buildConfidentialDocumentList(uploadedBy)
                    : buildNonConfidentialDocumentList(uploadedBy)
            );
            List<Element<RespondentStatement>> beforeRespondentStatement = respondentStatements;
            List<Element<RespondentStatement>> afterRespondentStatement = respondentStatements
                .stream()
                .map(rs -> element(rs.getId(), rs.getValue().toBuilder()
                    .supportingEvidenceBundle(rs.getValue().getSupportingEvidenceBundle().stream()
                        .map(seb -> element(seb.getId(), seb.getValue().toBuilder()
                            .document(PDF_DOCUMENT_3)
                            .build()))
                        .toList())
                    .build()))
                .toList();

            verifyNotificationTemplate(
                uploaderType,
                (caseData) ->  caseData.getRespondentStatements().addAll(beforeRespondentStatement),
                (caseData) ->  caseData.getRespondentStatements().addAll(afterRespondentStatement),
                notificationTypes, expectedDocumentNames);

            verifyCafcassNotificationTemplate(
                uploaderType,
                EMPTY_CASE_DATA_MODIFIER,
                (caseData) ->  caseData.getRespondentStatements().addAll(respondentStatements),
                toDocumentReferencesExtractor(respondentStatements),
                isNotifyingCafcass(notificationTypes) ? "• Respondent statement" : null,
                isNotifyingCafcass(notificationTypes) ? "Further documents for main application" : null);
        }

        @ParameterizedTest
        @ArgumentsSource(RespondentStatementConfidentialChangeArgs.class)
        void shouldNotSendNotificationWhenDocsAreRemoved(DocumentUploaderType uploaderType,
                                                         String uploadedBy,
                                                         boolean oldConfidential,
                                                         boolean newConfidential) {
            UUID respondentId = UUID.randomUUID();
            UUID elementId = UUID.randomUUID();
            UUID element2Id = UUID.randomUUID();
            List<Element<RespondentStatement>> beforeRespondentStatements =
                buildRespondentStatementsList(respondentId, List.of(
                    element(elementId, createDummyEvidenceBundle(CONFIDENTIAL_1, uploadedBy, oldConfidential,
                        PDF_DOCUMENT_1)),
                    element(element2Id, createDummyEvidenceBundle(CONFIDENTIAL_2, uploadedBy, oldConfidential,
                        PDF_DOCUMENT_2))));

            List<Element<RespondentStatement>> afterRespondentStatements =
                buildRespondentStatementsList(respondentId, List.of(element(elementId,
                    createDummyEvidenceBundle(CONFIDENTIAL_1, uploadedBy, newConfidential, PDF_DOCUMENT_1))));

            verifyNotificationTemplate(
                uploaderType,
                (caseData) ->  caseData.getRespondentStatements().addAll(beforeRespondentStatements),
                (caseData) ->  caseData.getRespondentStatements().addAll(afterRespondentStatements),
                Set.of(), null);

            verifyCafcassNotificationTemplate(
                uploaderType,
                (caseData) ->  caseData.getRespondentStatements().addAll(beforeRespondentStatements),
                (caseData) ->  caseData.getRespondentStatements().addAll(afterRespondentStatements),
                toDocumentReferencesExtractor(afterRespondentStatements),
                null, null);
        }

        @ParameterizedTest
        @ArgumentsSource(RespondentStatementConfidentialChangeArgs.class)
        void shouldNotSendNotificationWhenDocsAreTheSame(DocumentUploaderType uploaderType,
                                                         String uploadedBy,
                                                         boolean oldConfidential,
                                                         boolean newConfidential) {
            UUID respondentId = UUID.randomUUID();
            UUID elementId = UUID.randomUUID();
            UUID element2Id = UUID.randomUUID();
            List<Element<RespondentStatement>> respondentStatements =
                buildRespondentStatementsList(respondentId, List.of(
                    element(elementId, createDummyEvidenceBundle(CONFIDENTIAL_1, uploadedBy, oldConfidential,
                        PDF_DOCUMENT_1)),
                    element(element2Id, createDummyEvidenceBundle(CONFIDENTIAL_2, uploadedBy, oldConfidential,
                        PDF_DOCUMENT_2))));

            verifyNotificationTemplate(
                uploaderType,
                (caseData) -> caseData.getRespondentStatements().addAll(respondentStatements),
                (caseData) -> caseData.getRespondentStatements().addAll(respondentStatements),
                Set.of(), null);

            verifyCafcassNotificationTemplate(
                uploaderType,
                (caseData) -> caseData.getRespondentStatements().addAll(respondentStatements),
                (caseData) -> caseData.getRespondentStatements().addAll(respondentStatements),
                toDocumentReferencesExtractor(respondentStatements),
                null, null);
        }

        @ParameterizedTest
        @ArgumentsSource(RespondentStatementConfidentialChangeArgs.class)
        void shouldNotSendNotificationWhenConfidentialChanged(DocumentUploaderType uploaderType,
                                                              String uploadedBy,
                                                              boolean oldConfidential,
                                                              boolean newConfidential) {
            UUID respondentId = UUID.randomUUID();
            UUID elementId = UUID.randomUUID();
            List<Element<RespondentStatement>> beforeRespondentStatement =
                buildRespondentStatementsList(respondentId, List.of(element(elementId,
                createDummyEvidenceBundle(CONFIDENTIAL_1, uploadedBy, oldConfidential, PDF_DOCUMENT_1))));

            List<Element<RespondentStatement>> afterRespondentStatement =
                buildRespondentStatementsList(respondentId, List.of(element(elementId,
                createDummyEvidenceBundle(CONFIDENTIAL_1, uploadedBy, newConfidential, PDF_DOCUMENT_1))));

            verifyNotificationTemplate(
                uploaderType,
                (caseData) ->  caseData.getRespondentStatements().addAll(beforeRespondentStatement), // before
                (caseData) ->  caseData.getRespondentStatements().addAll(afterRespondentStatement),
                Set.of(), null);

            verifyCafcassNotificationTemplate(
                uploaderType,
                (caseData) -> caseData.getRespondentStatements().addAll(beforeRespondentStatement),
                (caseData) -> caseData.getRespondentStatements().addAll(afterRespondentStatement),
                toDocumentReferencesExtractor(afterRespondentStatement),
                null, null);
        }
    }

    static class ApplicationDocumentUploadTestArgs implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                of(DESIGNATED_LOCAL_AUTHORITY, LA_USER,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR, CAFCASS_REPRESENTATIVES),
                    List.of(BIRTH_CERTIFICATE.getLabel()), false),
                of(DESIGNATED_LOCAL_AUTHORITY, LA_USER,
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
                of(DESIGNATED_LOCAL_AUTHORITY, LA_USER, true, true),
                of(DESIGNATED_LOCAL_AUTHORITY, LA_USER, true, false),
                of(DESIGNATED_LOCAL_AUTHORITY, LA_USER, false, true),
                of(DESIGNATED_LOCAL_AUTHORITY, LA_USER, false, false)
            );
        }
    }

    @Nested
    class ApplicationDocumentUploadTests {

        private Function<CaseData, Set<DocumentReference>> toDocumentReferencesExtractor(
            List<Element<ApplicationDocument>> applicationDocuments) {
            return caseData -> unwrapElements(applicationDocuments).stream()
                .map(ad -> ad.getDocument()).collect(toSet());
        }

        @ParameterizedTest
        @ArgumentsSource(ApplicationDocumentUploadTestArgs.class)
        void shouldSendNotificationForNewUpload(DocumentUploaderType uploaderType,
                                                String uploadedBy,
                                                Set<DocumentUploadNotificationUserType> notificationTypes,
                                                List<String> expectedDocumentNames,
                                                boolean confidential) {
            List<Element<ApplicationDocument>> applicationDocuments =
                wrapElementsWithUUIDs(createDummyApplicationDocument("whatever", uploadedBy,
                    PDF_DOCUMENT_1, confidential, BIRTH_CERTIFICATE));
            verifyNotificationTemplate(
                uploaderType, EMPTY_CASE_DATA_MODIFIER,
                (caseData) ->  caseData.getApplicationDocuments().addAll(applicationDocuments),
                notificationTypes, expectedDocumentNames);
            verifyCafcassNotificationTemplate(
                uploaderType,
                EMPTY_CASE_DATA_MODIFIER,
                (caseData) ->  caseData.getApplicationDocuments().addAll(applicationDocuments),
                toDocumentReferencesExtractor(applicationDocuments),
                "• Birth certificate",
                "Further documents for main application");
        }

        @ParameterizedTest
        @ArgumentsSource(ApplicationDocumentUploadTestArgs.class)
        void shouldSendNotificationWhenDocsAreReplaced(DocumentUploaderType uploaderType,
                                                       String uploadedBy,
                                                       Set<DocumentUploadNotificationUserType> notificationTypes,
                                                       List<String> expectedDocumentNames,
                                                       boolean confidential) {
            UUID elementId = UUID.randomUUID();
            List<Element<ApplicationDocument>> beforeApplicationDocuments =
                List.of(element(elementId, createDummyApplicationDocument("whatever2", uploadedBy,
                    PDF_DOCUMENT_2, confidential, BIRTH_CERTIFICATE)));
            List<Element<ApplicationDocument>> afterApplicationDocuments =
                List.of(element(elementId, createDummyApplicationDocument("whatever1", uploadedBy,
                    PDF_DOCUMENT_1, confidential, BIRTH_CERTIFICATE)));

            verifyNotificationTemplate(
                uploaderType,
                (caseData) ->  caseData.getApplicationDocuments().addAll(beforeApplicationDocuments),
                (caseData) ->  caseData.getApplicationDocuments().addAll(afterApplicationDocuments),
                notificationTypes, expectedDocumentNames);

            verifyCafcassNotificationTemplate(
                uploaderType,
                (caseData) ->  caseData.getApplicationDocuments().addAll(beforeApplicationDocuments),
                (caseData) ->  caseData.getApplicationDocuments().addAll(afterApplicationDocuments),
                toDocumentReferencesExtractor(afterApplicationDocuments),
                "• Birth certificate",
                "Further documents for main application");
        }

        @ParameterizedTest
        @ArgumentsSource(ApplicationDocumentUploadTestArgs.class)
        void shouldNotSendNotificationWhenDocsAreRemoved(DocumentUploaderType uploaderType,
                                                         String uploadedBy,
                                                         Set<DocumentUploadNotificationUserType> notificationTypes,
                                                         List<String> expectedDocumentNames,
                                                         boolean confidential) {
            UUID elementId = UUID.randomUUID();
            List<Element<ApplicationDocument>> applicationDocuments =
                List.of(element(elementId, createDummyApplicationDocument("whatever2", uploadedBy,
                    PDF_DOCUMENT_2, confidential, BIRTH_CERTIFICATE)));

            verifyNotificationTemplate(
                uploaderType,
                (caseData) ->  caseData.getApplicationDocuments().addAll(applicationDocuments),
                EMPTY_CASE_DATA_MODIFIER,
                Set.of(), null);

            verifyCafcassNotificationTemplate(
                uploaderType,
                (caseData) ->  caseData.getApplicationDocuments().addAll(applicationDocuments),
                EMPTY_CASE_DATA_MODIFIER,
                toDocumentReferencesExtractor(applicationDocuments),
                null,
                null);
        }

        @ParameterizedTest
        @ArgumentsSource(ApplicationDocumentUploadTestArgs.class)
        void shouldNotSendNotificationWhenDocsAreTheSame(DocumentUploaderType uploaderType,
                                                         String uploadedBy,
                                                         Set<DocumentUploadNotificationUserType> notificationTypes,
                                                         List<String> expectedDocumentNames,
                                                         boolean confidential) {
            UUID elementId = UUID.randomUUID();
            List<Element<ApplicationDocument>> applicationDocuments =
                List.of(element(elementId, createDummyApplicationDocument("whatever2", uploadedBy,
                    PDF_DOCUMENT_2, confidential, BIRTH_CERTIFICATE)));

            verifyNotificationTemplate(
                uploaderType,
                (caseData) ->  caseData.getApplicationDocuments().addAll(applicationDocuments),
                (caseData) ->  caseData.getApplicationDocuments().addAll(applicationDocuments),
                Set.of(), null);

            verifyCafcassNotificationTemplate(
                uploaderType,
                (caseData) ->  caseData.getApplicationDocuments().addAll(applicationDocuments),
                (caseData) ->  caseData.getApplicationDocuments().addAll(applicationDocuments),
                toDocumentReferencesExtractor(applicationDocuments),
                null,
                null);
        }

        @ParameterizedTest
        @ArgumentsSource(ApplicationDocumentConfidentialChangeArgs.class)
        void shouldNotSendNotificationWhenConfidentialChanged(DocumentUploaderType uploaderType,
                                                              String uploadedBy,
                                                              boolean oldConfidential,
                                                              boolean newConfidential) {
            UUID elementId = UUID.randomUUID();
            List<Element<ApplicationDocument>> beforeApplicationDocuments =
                List.of(element(elementId, createDummyApplicationDocument("whatever", uploadedBy,
                    PDF_DOCUMENT_1, oldConfidential, BIRTH_CERTIFICATE)));
            List<Element<ApplicationDocument>> afterApplicationDocuments =
                List.of(element(elementId, createDummyApplicationDocument("whatever", uploadedBy,
                    PDF_DOCUMENT_1, newConfidential, BIRTH_CERTIFICATE)));

            verifyNotificationTemplate(
                uploaderType,
                (caseData) ->  caseData.getApplicationDocuments().addAll(beforeApplicationDocuments),
                (caseData) ->  caseData.getApplicationDocuments().addAll(afterApplicationDocuments),
                Set.of(), null);

            verifyCafcassNotificationTemplate(
                uploaderType,
                (caseData) ->  caseData.getApplicationDocuments().addAll(beforeApplicationDocuments),
                (caseData) ->  caseData.getApplicationDocuments().addAll(afterApplicationDocuments),
                toDocumentReferencesExtractor(beforeApplicationDocuments),
                null,
                null);
        }
    }

    static class AnyOtherDocumentUploadTestArgs implements ArgumentsProvider {

        static final boolean HEARING_RELATED_YES = true;
        static final boolean HEARING_RELATED_NO = false;

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                // By DESIGNATED_LOCAL_AUTHORITY
                of(DESIGNATED_LOCAL_AUTHORITY,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR, CAFCASS_REPRESENTATIVES),
                    NON_CONFIDENTIAL,
                    buildNonConfidentialDocumentList(LA_USER),
                    HEARING_RELATED_NO),
                of(DESIGNATED_LOCAL_AUTHORITY,
                    Set.of(ALL_LAS, CAFCASS_REPRESENTATIVES),
                    CONFIDENTIAL,
                    buildConfidentialDocumentList(LA_USER),
                    HEARING_RELATED_NO),
                of(DESIGNATED_LOCAL_AUTHORITY,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR, CAFCASS_REPRESENTATIVES),
                    NON_CONFIDENTIAL,
                    buildHearingFurtherEvidenceBundle(buildNonConfidentialDocumentList(LA_USER)),
                    HEARING_RELATED_YES),
                of(DESIGNATED_LOCAL_AUTHORITY,
                    Set.of(ALL_LAS, CAFCASS_REPRESENTATIVES),
                    CONFIDENTIAL,
                    buildHearingFurtherEvidenceBundle(buildConfidentialDocumentList(LA_USER)),
                    HEARING_RELATED_YES),
                // Not notifying CAFCASS and CAFCASS_REPRESENTATIVES if document type is
                // NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE
                of(DESIGNATED_LOCAL_AUTHORITY,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR),
                    NON_CONFIDENTIAL,
                    buildNonConfidentialDocumentList(LA_USER, NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE),
                    HEARING_RELATED_NO),
                of(DESIGNATED_LOCAL_AUTHORITY,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR),
                    NON_CONFIDENTIAL,
                    buildHearingFurtherEvidenceBundle(
                        buildNonConfidentialDocumentList(LA_USER,
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
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR, CAFCASS_REPRESENTATIVES),
                    NON_CONFIDENTIAL,
                    buildNonConfidentialDocumentList(HMCTS_USER),
                    HEARING_RELATED_NO),
                of(HMCTS,
                    Set.of(),
                    CONFIDENTIAL,
                    buildConfidentialDocumentList(HMCTS_USER),
                    HEARING_RELATED_NO),
                of(HMCTS,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR, CAFCASS_REPRESENTATIVES),
                    NON_CONFIDENTIAL,
                    buildHearingFurtherEvidenceBundle(buildNonConfidentialDocumentList(HMCTS_USER)),
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
                    buildNonConfidentialDocumentList(HMCTS_USER, NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE),
                    HEARING_RELATED_NO),
                of(HMCTS,
                    Set.of(),
                    CONFIDENTIAL,
                    buildConfidentialDocumentList(HMCTS_USER, NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE),
                    HEARING_RELATED_NO),
                of(HMCTS,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR),
                    NON_CONFIDENTIAL,
                    buildHearingFurtherEvidenceBundle(
                        buildNonConfidentialDocumentList(HMCTS_USER,
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
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR, CAFCASS_REPRESENTATIVES),
                    NON_CONFIDENTIAL,
                    buildNonConfidentialDocumentList(REP_USER),
                    HEARING_RELATED_NO),
                of(SOLICITOR,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR, CAFCASS_REPRESENTATIVES),
                    NON_CONFIDENTIAL,
                    buildHearingFurtherEvidenceBundle(buildNonConfidentialDocumentList(REP_USER)),
                    HEARING_RELATED_YES),
                // Not notifying CAFCASS if document type is NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE
                of(SOLICITOR,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR),
                    NON_CONFIDENTIAL,
                    buildNonConfidentialDocumentList(REP_USER, NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE),
                    HEARING_RELATED_NO),
                of(SOLICITOR,
                    Set.of(ALL_LAS, CHILD_SOLICITOR, RESPONDENT_SOLICITOR),
                    NON_CONFIDENTIAL,
                    buildHearingFurtherEvidenceBundle(
                        buildNonConfidentialDocumentList(REP_USER, NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE)),
                    HEARING_RELATED_YES)
            );
        }
    }

    @Nested
    class AnyOtherDocumentUploadTests {
        @SuppressWarnings("unchecked")
        private Consumer<CaseData> toCaseDataModifier(List<?> documents,
                                                      DocumentUploaderType uploaderType,
                                                      boolean isRelatingToHearing) {
            return isRelatingToHearing ? (caseData) -> caseData.getHearingFurtherEvidenceDocuments()
                .addAll((List<Element<HearingFurtherEvidenceBundle>>) documents)
                : (caseData) -> document(caseData, uploaderType)
                .addAll((List<Element<SupportingEvidenceBundle>>) documents);
        }

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

        @SuppressWarnings("unchecked")
        Set<DocumentReference> getExpectedDocumentReferences(CaseData caseData,
                                                             DocumentUploaderType uploaderType,
                                                             boolean isRelatingToHearing) {
            if (isRelatingToHearing) {
                return unwrapElements(caseData.getHearingFurtherEvidenceDocuments())
                    .stream()
                    .map(sfvb -> unwrapElements(sfvb.getSupportingEvidenceBundle()))
                    .flatMap(List::stream)
                    .map(f -> f.getDocument())
                    .collect(toSet());
            } else {
                return unwrapElements(document(caseData, uploaderType)).stream()
                    .map(f -> f.getDocument()).collect(toSet());
            }
        }

        @ParameterizedTest
        @ArgumentsSource(AnyOtherDocumentUploadTestArgs.class)
        @SuppressWarnings("unchecked")
        void shouldSendNotificationForNewUpload(DocumentUploaderType uploaderType,
                                                Set<DocumentUploadNotificationUserType> notificationTypes,
                                                List<String> expectedDocumentNames,
                                                List<?> documents,
                                                boolean isRelatingToHearing) {
            verifyNotificationTemplate(
                uploaderType,
                EMPTY_CASE_DATA_MODIFIER,
                toCaseDataModifier(documents, uploaderType, isRelatingToHearing),
                notificationTypes, expectedDocumentNames);
            verifyCafcassNotificationTemplate(
                uploaderType,
                EMPTY_CASE_DATA_MODIFIER,
                toCaseDataModifier(documents, uploaderType, isRelatingToHearing),
                (caseData) -> getExpectedDocumentReferences(caseData, uploaderType, isRelatingToHearing),
                isNotifyingCafcass(notificationTypes) ? "• Child's guardian reports\n• Child's guardian reports" : null,
                isNotifyingCafcass(notificationTypes) ? "Further documents for main application" : null);
        }

        @ParameterizedTest
        @ArgumentsSource(AnyOtherDocumentUploadTestArgs.class)
        @SuppressWarnings("unchecked")
        void shouldSendNotificationWhenDocsAreReplaced(DocumentUploaderType uploaderType,
                                                       Set<DocumentUploadNotificationUserType> notificationTypes,
                                                       List<String> expectedDocumentNames,
                                                       List<?> documents,
                                                       boolean isRelatingToHearing) {
            List<?> beforeDocument = documents;
            List<?> afterDocuments;
            if (isRelatingToHearing) {
                afterDocuments = ((List<Element<HearingFurtherEvidenceBundle>>) documents)
                    .stream()
                    .map(hfvb -> element(hfvb.getId(), hfvb.getValue().toBuilder()
                        .supportingEvidenceBundle(hfvb.getValue().getSupportingEvidenceBundle().stream()
                            .map(seb -> element(seb.getId(), seb.getValue().toBuilder()
                                    .document(PDF_DOCUMENT_3)
                                .build()))
                            .toList())
                        .build()))
                    .toList();
            } else {
                afterDocuments = ((List<Element<SupportingEvidenceBundle>>) documents).stream()
                    .map(seb -> element(seb.getId(), seb.getValue().toBuilder()
                        .document(PDF_DOCUMENT_3)
                        .build()))
                    .toList();
            }

            verifyNotificationTemplate(
                uploaderType,
                toCaseDataModifier(beforeDocument, uploaderType, isRelatingToHearing),
                toCaseDataModifier(afterDocuments, uploaderType, isRelatingToHearing),
                notificationTypes, expectedDocumentNames);

            verifyCafcassNotificationTemplate(
                uploaderType,
                toCaseDataModifier(beforeDocument, uploaderType, isRelatingToHearing),
                toCaseDataModifier(afterDocuments, uploaderType, isRelatingToHearing),
                (caseData) -> getExpectedDocumentReferences(caseData, uploaderType, isRelatingToHearing),
                isNotifyingCafcass(notificationTypes) ? "• Child's guardian reports\n• Child's guardian reports" : null,
                isNotifyingCafcass(notificationTypes) ? "Further documents for main application" : null);
        }

        @ParameterizedTest
        @ArgumentsSource(AnyOtherDocumentUploadTestArgs.class)
        @SuppressWarnings("unchecked")
        void shouldNotSendNotificationWhenDocsAreRemoved(DocumentUploaderType uploaderType,
                                                         Set<DocumentUploadNotificationUserType> notificationTypes,
                                                         List<String> expectedDocumentNames,
                                                         List<?> documents,
                                                         boolean isRelatingToHearing) {
            verifyNotificationTemplate(
                uploaderType,
                toCaseDataModifier(documents, uploaderType, isRelatingToHearing),
                EMPTY_CASE_DATA_MODIFIER,
                Set.of(), null);
            verifyCafcassNotificationTemplate(
                uploaderType,
                toCaseDataModifier(documents, uploaderType, isRelatingToHearing),
                EMPTY_CASE_DATA_MODIFIER,
                (caseData) -> getExpectedDocumentReferences(caseData, uploaderType, isRelatingToHearing),
                null,
                null);
        }

        @ParameterizedTest
        @ArgumentsSource(AnyOtherDocumentUploadTestArgs.class)
        @SuppressWarnings("unchecked")
        void shouldNotSendNotificationWhenDocsAreTheSame(DocumentUploaderType uploaderType,
                                                         Set<DocumentUploadNotificationUserType> notificationTypes,
                                                         List<String> expectedDocumentNames,
                                                         List<?> documents,
                                                         boolean isRelatingToHearing) {
            verifyNotificationTemplate(
                uploaderType,
                toCaseDataModifier(documents, uploaderType, isRelatingToHearing),
                toCaseDataModifier(documents, uploaderType, isRelatingToHearing),
                Set.of(), null);
            verifyCafcassNotificationTemplate(
                uploaderType,
                toCaseDataModifier(documents, uploaderType, isRelatingToHearing),
                toCaseDataModifier(documents, uploaderType, isRelatingToHearing),
                (caseData) -> getExpectedDocumentReferences(caseData, uploaderType, isRelatingToHearing),
                null,
                null);
        }

        @ParameterizedTest
        @ArgumentsSource(AnyOtherDocumentUploadTestArgs.class)
        @SuppressWarnings("unchecked")
        void shouldNotSendNotificationWhenConfidentialChanged(DocumentUploaderType uploaderType,
                                                              Set<DocumentUploadNotificationUserType> notificationTypes,
                                                              List<String> expectedDocumentNames,
                                                              List<?> documents,
                                                              boolean isRelatingToHearing) {
            List<?> beforeDocument = documents;
            List<?> afterDocuments;
            if (isRelatingToHearing) {
                afterDocuments = ((List<Element<HearingFurtherEvidenceBundle>>) documents)
                    .stream()
                    .map(hfvb -> element(hfvb.getId(), hfvb.getValue().toBuilder()
                        .supportingEvidenceBundle(hfvb.getValue().getSupportingEvidenceBundle().stream()
                            .map(seb -> element(seb.getId(), seb.getValue().toBuilder()
                                .confidential(seb.getValue().getConfidential() != null
                                    ? null : List.of("CONFIDENTIAL"))
                                .build()))
                            .toList())
                        .build()))
                    .toList();
            } else {
                afterDocuments = ((List<Element<SupportingEvidenceBundle>>) documents).stream()
                    .map(seb -> element(seb.getId(), seb.getValue().toBuilder()
                        .confidential(seb.getValue().getConfidential() != null
                            ? null : List.of("CONFIDENTIAL"))
                        .build()))
                    .toList();
            }

            verifyNotificationTemplate(uploaderType,
                toCaseDataModifier(documents, uploaderType, isRelatingToHearing),
                toCaseDataModifier(afterDocuments, uploaderType, isRelatingToHearing),
                Set.of(), null);
            verifyCafcassNotificationTemplate(
                uploaderType,
                toCaseDataModifier(documents, uploaderType, isRelatingToHearing),
                toCaseDataModifier(afterDocuments, uploaderType, isRelatingToHearing),
                (caseData) -> getExpectedDocumentReferences(caseData, uploaderType, isRelatingToHearing),
                null,
                null);
        }
    }
}
