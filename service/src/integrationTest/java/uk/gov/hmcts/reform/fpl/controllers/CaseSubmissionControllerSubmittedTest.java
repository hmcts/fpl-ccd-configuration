package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fpl.docmosis.DocmosisHelper;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeType;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.ReturnApplication;
import uk.gov.hmcts.reform.fpl.model.UnregisteredOrganisation;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.SharedNotifyTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.representative.RegisteredRepresentativeSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.representative.UnregisteredRepresentativeSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseCafcassTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseHmctsTemplate;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.email.EmailService;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;
import uk.gov.hmcts.reform.fpl.service.translation.TranslationRequestFormCreationService;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.CAFCASS_WELSH_COURT;
import static uk.gov.hmcts.reform.fpl.Constants.COURT_1;
import static uk.gov.hmcts.reform.fpl.Constants.COURT_3A;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_CAFCASS_COURT;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_LA_COURT;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_3_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_3_COURT_A_NAME;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_3_NAME;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.AMENDED_APPLICATION_RETURNED_ADMIN_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.AMENDED_APPLICATION_RETURNED_CAFCASS_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.OUTSOURCED_CASE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType.CONTACT_WITH_NAMED_PERSON;
import static uk.gov.hmcts.reform.fpl.controllers.ReturnApplicationController.RETURN_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.ENGLISH_TO_WELSH;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.ReturnedApplicationReasons.INCOMPLETE;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.State.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.CaseConverter.MAP_TYPE;
import static uk.gov.hmcts.reform.fpl.testingsupport.IntegrationTestConstants.CAFCASS_EMAIL;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkThat;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithUUIDs;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;

@WebMvcTest(CaseSubmissionController.class)
@OverrideAutoConfiguration(enabled = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CaseSubmissionControllerSubmittedTest extends AbstractCallbackTest {
    private static final String CTSC_EMAIL = "FamilyPublicLaw+ctsc@gmail.com";
    private static final String SOLICITOR_EMAIL = "solicitor@email.com";
    private static final String SOLICITOR_FIRST_NAME = "John";
    private static final String SOLICITOR_LAST_NAME = "Smith";
    private static final String RESPONDENT_FIRST_NAME = "David";
    private static final String RESPONDENT_LAST_NAME = "Jones";
    private static final String DISPLAY_AMOUNT_TO_PAY = "displayAmountToPay";
    private static final String SURVEY_LINK = "https://fake.survey.url";
    private static final Long CASE_ID = 1234567890123456L;
    private static final String CASE_NAME = "test case name1";
    private static final byte[] APPLICATION_BINARY = DOCUMENT_CONTENT;
    private static final byte[] DOCUMENT_PDF_BINARIES = readBytes("documents/document1.pdf");
    private static final DocmosisDocument DOCMOSIS_PDF_DOCUMENT = testDocmosisDocument(DOCUMENT_PDF_BINARIES)
        .toBuilder().documentTitle("pdf.pdf").build();


    @MockBean
    private PaymentService paymentService;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    DocmosisHelper docmosisHelper;

    @MockBean
    TranslationRequestFormCreationService translationRequestFormCreationService;

    @MockBean
    private CafcassNotificationService cafcassNotificationService;


    @Captor
    private ArgumentCaptor<CafcassRequestEmailContentProvider> cafcassRequestEmailContentProviderArgumentCaptor;

    CaseSubmissionControllerSubmittedTest() {
        super("case-submission");
    }

    @BeforeEach
    void init() {
        when(documentDownloadService.downloadDocument(any())).thenReturn(DOCUMENT_CONTENT);
        when(translationRequestFormCreationService.buildTranslationRequestDocuments(any()))
            .thenReturn(DOCMOSIS_PDF_DOCUMENT);
        when(documentDownloadService.downloadDocument(any())).thenReturn(APPLICATION_BINARY);
        when(docmosisHelper.extractPdfContent(APPLICATION_BINARY)).thenReturn("Some content");
        when(featureToggleService.isWATaskEmailsEnabled()).thenReturn(true);
    }

    @Test
    void shouldBuildNotificationTemplatesWithCompleteValues() {
        final Map<String, Object> expectedHmctsParameters =
                toMap(getExpectedHmctsParameters(true, LOCAL_AUTHORITY_3_COURT_A_NAME, LOCAL_AUTHORITY_3_NAME));

        final Map<String, Object> completeCafcassParameters =
                toMap(getExpectedCafcassParameters(true, CAFCASS_WELSH_COURT, LOCAL_AUTHORITY_3_NAME));

        CaseDetails caseDetails = populatedCaseDetails(Map.of("id", CASE_ID, "caseLocalAuthority", "test3"));
        caseDetails.getData().put(DISPLAY_AMOUNT_TO_PAY, YES.getValue());
        caseDetails.getData().put("submittedForm", DocumentReference.builder().binaryUrl("/testUrl").build());
        caseDetails.getData().put("representativeType", RepresentativeType.LOCAL_AUTHORITY);

        postSubmittedEvent(buildCallbackRequest(caseDetails, OPEN));

        checkUntil(() -> {
            verify(notificationClient).sendEmail(
                HMCTS_COURT_SUBMISSION_TEMPLATE,
                COURT_3A.getEmail(),
                expectedHmctsParameters,
                notificationReference(CASE_ID));

            verify(notificationClient).sendEmail(
                CAFCASS_SUBMISSION_TEMPLATE,
                CAFCASS_EMAIL,
                completeCafcassParameters,
                notificationReference(CASE_ID));
        });

        checkThat(() -> verifyNoMoreInteractions(notificationClient));
        verifyTaskListUpdated();

        verify(coreCaseDataService).performPostSubmitCallback(eq(CASE_ID),
            eq("internal-update-case-summary"), any());
        verify(cafcassNotificationService, never()).sendEmail(
                isA(CaseData.class), any(), any(), any()
        );
    }

    @Test
    void shouldBuildNotificationTemplatesWithCompleteValuesNotifyingCafcassBySendGrindOnly() {
        final Map<String, Object> expectedHmctsParameters =
                toMap(getExpectedHmctsParameters(true, DEFAULT_LA_COURT, LOCAL_AUTHORITY_1_NAME));

        final Map<String, Object> completeCafcassParameters =
                toMap(getExpectedCafcassParameters(true, DEFAULT_CAFCASS_COURT, LOCAL_AUTHORITY_1_NAME));

        CaseDetails caseDetails = populatedCaseDetails(Map.of("id", CASE_ID));
        caseDetails.getData().put(DISPLAY_AMOUNT_TO_PAY, YES.getValue());
        caseDetails.getData().put("submittedForm", DocumentReference.builder().binaryUrl("/testUrl").build());
        caseDetails.getData().put("representativeType", RepresentativeType.LOCAL_AUTHORITY);

        postSubmittedEvent(buildCallbackRequest(caseDetails, OPEN));

        checkUntil(() -> {
            verify(notificationClient).sendEmail(
                    HMCTS_COURT_SUBMISSION_TEMPLATE,
                    COURT_1.getEmail(),
                    expectedHmctsParameters,
                    notificationReference(CASE_ID));

            verify(notificationClient, never()).sendEmail(
                    CAFCASS_SUBMISSION_TEMPLATE,
                    CAFCASS_EMAIL,
                    completeCafcassParameters,
                    notificationReference(CASE_ID));
        });

        checkThat(() -> verifyNoMoreInteractions(notificationClient));
        verifyTaskListUpdated();

        verify(coreCaseDataService).performPostSubmitCallback(eq(CASE_ID),
                eq("internal-update-case-summary"), any());
        verifyCafcassOrderNotification();
    }


    @Test
    void shouldNotifyRegisteredSolicitorsWhenCaseIsSubmitted() {
        CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .representativeType(RepresentativeType.LOCAL_AUTHORITY)
            .respondents1(wrapElements(Respondent.builder()
                .legalRepresentation("Yes")
                .party(RespondentParty.builder()
                    .firstName(RESPONDENT_FIRST_NAME).lastName(RESPONDENT_LAST_NAME).build())
                .solicitor(RespondentSolicitor.builder()
                    .firstName("First").lastName("Representative")
                    .email(SOLICITOR_EMAIL)
                    .organisation(Organisation.builder().organisationID("123").build()).build())
                .build()))
            .id(CASE_ID)
            .caseName(CASE_NAME)
            .displayAmountToPay(YES.getValue())
            .c110A(uk.gov.hmcts.reform.fpl.model.group.C110A.builder()
                .submittedForm(DocumentReference.builder().binaryUrl("testUrl").build())
                .build())
            .build();

        final Map<String, Object> registeredSolicitorParameters = toMap(getExpectedRegisteredSolicitorParameters());

        postSubmittedEvent(buildCallbackRequest(asCaseDetails(caseData), OPEN));

        checkUntil(() ->
            verify(notificationClient).sendEmail(
                REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE,
                SOLICITOR_EMAIL,
                registeredSolicitorParameters,
                notificationReference(CASE_ID)));
        verifyCafcassOrderNotification();
    }

    @Test
    void shouldNotifyTranslationTeamWhenCaseIsSubmittedAndTranslationRequested() {
        CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .representativeType(RepresentativeType.LOCAL_AUTHORITY)
            .respondents1(wrapElements(Respondent.builder()
                .legalRepresentation("Yes")
                .party(RespondentParty.builder()
                    .firstName(RESPONDENT_FIRST_NAME).lastName(RESPONDENT_LAST_NAME).build())
                .solicitor(RespondentSolicitor.builder()
                    .firstName("First").lastName("Representative")
                    .email(SOLICITOR_EMAIL)
                    .organisation(Organisation.builder().organisationID("123").build()).build())
                .build()))
            .id(CASE_ID)
            .caseName(CASE_NAME)
            .displayAmountToPay(YES.getValue())
            .c110A(uk.gov.hmcts.reform.fpl.model.group.C110A.builder()
                .submittedForm(DocumentReference.builder().binaryUrl("testUrl")
                    .filename("app.pdf")
                    .build())
                .submittedFormTranslationRequirements(ENGLISH_TO_WELSH)
                .build())
            .build();

        postSubmittedEvent(buildCallbackRequest(asCaseDetails(caseData), OPEN));

        verifyEmailSentToTranslation();
        verifyCafcassOrderNotification();
    }

    @Test
    void shouldNotNotifyTranslationTeamWhenCaseIsSubmittedAndTranslationNotRequested() {
        CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .representativeType(RepresentativeType.LOCAL_AUTHORITY)
            .respondents1(wrapElements(Respondent.builder()
                .legalRepresentation("Yes")
                .party(RespondentParty.builder()
                    .firstName(RESPONDENT_FIRST_NAME).lastName(RESPONDENT_LAST_NAME).build())
                .solicitor(RespondentSolicitor.builder()
                    .firstName("First").lastName("Representative")
                    .email(SOLICITOR_EMAIL)
                    .organisation(Organisation.builder().organisationID("123").build()).build())
                .build()))
            .id(CASE_ID)
            .caseName(CASE_NAME)
            .displayAmountToPay(YES.getValue())
            .c110A(uk.gov.hmcts.reform.fpl.model.group.C110A.builder()
                .submittedForm(DocumentReference.builder().binaryUrl("testUrl")
                    .filename("app.pdf")
                    .build())
                .build())
            .build();

        postSubmittedEvent(buildCallbackRequest(asCaseDetails(caseData), OPEN));

        verifyNoMoreNotificationsSentToTranslationTeam();
        verifyCafcassOrderNotification();
    }

    @Test
    void shouldUpdateTheCaseManagementSummary() {
        CaseDetails caseDetails = populatedCaseDetails(Map.of("id", CASE_ID));
        caseDetails.getData().put("representativeType", RepresentativeType.LOCAL_AUTHORITY);

        postSubmittedEvent(buildCallbackRequest(caseDetails, OPEN));

        verify(coreCaseDataService).performPostSubmitCallback(eq(CASE_ID),
            eq("internal-update-case-summary"), any());
    }

    @Test
    void shouldBuildNotificationTemplatesWithValuesMissingInCallback() {
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(NO);
        caseDetails.getData().put("caseLocalAuthority", LOCAL_AUTHORITY_3_CODE);
        caseDetails.getData().put("caseLocalAuthorityName", LOCAL_AUTHORITY_3_NAME);

        caseDetails.getData().put(DISPLAY_AMOUNT_TO_PAY, YES.getValue());
        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);

        postSubmittedEvent(callbackRequest);

        Map<String, Object> expectedIncompleteHmctsParameters =
                toMap(getExpectedHmctsParameters(false, LOCAL_AUTHORITY_3_COURT_A_NAME, LOCAL_AUTHORITY_3_NAME));

        checkUntil(() -> {
            verify(notificationClient).sendEmail(
                HMCTS_COURT_SUBMISSION_TEMPLATE,
                COURT_3A.getEmail(),
                expectedIncompleteHmctsParameters,
                notificationReference(CASE_ID));

            verify(notificationClient).sendEmail(
                CAFCASS_SUBMISSION_TEMPLATE,
                CAFCASS_EMAIL,
                getExpectedCafcassParameters(false, CAFCASS_WELSH_COURT, LOCAL_AUTHORITY_3_NAME),
                notificationReference(CASE_ID));
        });

        checkThat(() -> verifyNoMoreInteractions(notificationClient));
        verify(cafcassNotificationService, never()).sendEmail(
                isA(CaseData.class), any(), any(), any()
        );
    }

    @Test
    void shouldSendNotificationToCtscAdminWhenCtscIsEnabledWithinCaseDetails() {
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);

        postSubmittedEvent(callbackRequest);

        Map<String, Object> expectedIncompleteHmctsParameters =
                toMap(getExpectedHmctsParameters(false, DEFAULT_LA_COURT, LOCAL_AUTHORITY_1_NAME));

        checkUntil(() ->
            verify(notificationClient).sendEmail(
                HMCTS_COURT_SUBMISSION_TEMPLATE,
                CTSC_EMAIL,
                expectedIncompleteHmctsParameters,
                notificationReference(CASE_ID)
            ));

        checkThat(() ->
            verify(notificationClient, never()).sendEmail(
                HMCTS_COURT_SUBMISSION_TEMPLATE,
                COURT_1.getEmail(),
                expectedIncompleteHmctsParameters,
                notificationReference(CASE_ID)
            ));
        verifyCafcassOrderNotification();
    }

    @Test
    void shouldNotSendNotificationToCtscAdminWhenToggledOff() throws NotificationClientException {
        when(featureToggleService.isWATaskEmailsEnabled()).thenReturn(false);
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);

        postSubmittedEvent(callbackRequest);

        Map<String, Object> expectedIncompleteHmctsParameters =
            toMap(getExpectedHmctsParameters(false, DEFAULT_LA_COURT, LOCAL_AUTHORITY_1_NAME));

        verify(notificationClient, never()).sendEmail(
            HMCTS_COURT_SUBMISSION_TEMPLATE,
            CTSC_EMAIL,
            expectedIncompleteHmctsParameters,
            notificationReference(CASE_ID)
        );
    }

    @Test
    void shouldNotifyManagedLAWhenCaseCreatedOnBehalfOfLA() {
        CaseData caseData = CaseData.builder()
            .representativeType(RepresentativeType.LOCAL_AUTHORITY)
            .outsourcingPolicy(OrganisationPolicy.builder()
                .organisation(Organisation.builder()
                    .organisationID("ORG1")
                    .organisationName("Third party")
                    .build())
                .build())
            .caseLocalAuthority(LOCAL_AUTHORITY_2_CODE)
            .localAuthorities(wrapElementsWithUUIDs(LocalAuthority.builder()
                .id(LOCAL_AUTHORITY_2_CODE)
                .designated(YES.getValue())
                .email(LOCAL_AUTHORITY_2_INBOX)
                .build()))
            .id(CASE_ID)
            .build();

        CallbackRequest callbackRequest = buildCallbackRequest(asCaseDetails(caseData), OPEN);

        postSubmittedEvent(callbackRequest);

        checkUntil(() -> verify(notificationClient).sendEmail(
            eq(OUTSOURCED_CASE_TEMPLATE), eq(LOCAL_AUTHORITY_2_INBOX),
            anyMap(), eq(notificationReference(CASE_ID))));
    }

    @Test
    void shouldNotifyUnregisteredSolicitorWhenUnregisteredOrganisationDetailsProvided() {
        Respondent respondent = Respondent.builder()
            .legalRepresentation(YES.getValue())
            .party(RespondentParty.builder().firstName(RESPONDENT_FIRST_NAME).lastName(RESPONDENT_LAST_NAME).build())
            .solicitor(RespondentSolicitor.builder()
                .firstName(SOLICITOR_FIRST_NAME)
                .lastName(SOLICITOR_LAST_NAME)
                .email(SOLICITOR_EMAIL)
                .unregisteredOrganisation(UnregisteredOrganisation.builder()
                    .name("Unregistered Org Name")
                    .build())
                .build()).build();

        CaseDetails caseDetails = populatedCaseDetails(Map.of("id", CASE_ID));
        caseDetails.getData().put("respondents1", wrapElements(respondent));
        caseDetails.getData().put("representativeType", RepresentativeType.LOCAL_AUTHORITY);

        postSubmittedEvent(buildCallbackRequest(caseDetails, OPEN));

        Map<String, Object> expectedParameters = mapper.convertValue(
            UnregisteredRepresentativeSolicitorTemplate.builder()
                .ccdNumber("1234-5678-9012-3456")
                .localAuthority(LOCAL_AUTHORITY_1_NAME)
                .clientFullName(String.format("%s %s", RESPONDENT_FIRST_NAME, RESPONDENT_LAST_NAME))
                .childLastName("Reeves")
                .caseName("test")
                .build(),
            MAP_TYPE
        );

        checkUntil(() ->
            verify(notificationClient).sendEmail(
                UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE,
                SOLICITOR_EMAIL,
                expectedParameters,
                notificationReference(CASE_ID)
            ));
        verifyCafcassOrderNotification();
    }

    @Test
    void shouldNotNotifyUnregisteredSolicitorWhenUnregisteredOrganisationDetailsNotProvided() {
        Respondent respondent = Respondent.builder()
            .legalRepresentation(YES.getValue())
            .party(RespondentParty.builder()
                .firstName(RESPONDENT_FIRST_NAME)
                .lastName(RESPONDENT_LAST_NAME).build())
            .solicitor(RespondentSolicitor.builder()
                .firstName(SOLICITOR_FIRST_NAME)
                .lastName(SOLICITOR_LAST_NAME)
                .email(SOLICITOR_EMAIL)
                .build()).build();

        CaseDetails caseDetails = populatedCaseDetails(Map.of("id", CASE_ID));
        caseDetails.getData().put("respondents1", wrapElements(respondent));
        caseDetails.getData().put("representativeType", RepresentativeType.LOCAL_AUTHORITY);

        postSubmittedEvent(buildCallbackRequest(caseDetails, OPEN));

        String expectedSalutation = String.format("Dear %s %s", SOLICITOR_FIRST_NAME, SOLICITOR_LAST_NAME);
        String expectedName = String.format("%s %s", RESPONDENT_FIRST_NAME, RESPONDENT_LAST_NAME);

        Map<String, Object> expectedUnregisteredSolicitorParameters = toMap(
            RegisteredRepresentativeSolicitorTemplate.builder()
                .salutation(expectedSalutation)
                .clientFullName(expectedName)
                .localAuthority(LOCAL_AUTHORITY_1_NAME)
                .build());

        checkUntil(() ->
            verify(notificationClient, never()).sendEmail(
                UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE,
                SOLICITOR_EMAIL,
                expectedUnregisteredSolicitorParameters,
                notificationReference(CASE_ID)
            ));
        verifyCafcassOrderNotification();
    }

    @Test
    void shouldMakePaymentOfAnOpenCaseWhenAmountToPayWasDisplayed() {
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
        caseDetails.getData().put(DISPLAY_AMOUNT_TO_PAY, YES.getValue());
        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);

        postSubmittedEvent(callbackRequest);

        checkUntil(() -> verify(paymentService).makePaymentForCaseOrders(caseConverter.convert(caseDetails)));
        verifyCafcassOrderNotification();
    }

    @Test
    void shouldNotMakePaymentOfAnOpenCaseWhenAmountToPayWasNotDisplayed() {
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
        caseDetails.getData().put(DISPLAY_AMOUNT_TO_PAY, NO.getValue());
        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);

        postSubmittedEvent(callbackRequest);

        checkThat(() -> verify(paymentService, never()).makePaymentForCaseOrders(any()));
        verifyCafcassOrderNotification();
    }

    @Test
    void shouldSendFailedPaymentNotificationOnPaymentsApiException() {
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
        caseDetails.getData().put(DISPLAY_AMOUNT_TO_PAY, YES.getValue());
        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);

        doThrow(new PaymentsApiException("", new Throwable())).when(paymentService)
            .makePaymentForCaseOrders(any(CaseData.class));

        postSubmittedEvent(callbackRequest);

        checkUntil(() -> {
            verify(notificationClient).sendEmail(
                APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA,
                LOCAL_AUTHORITY_1_INBOX,
                expectedLocalAuthorityNotificationParameters(),
                notificationReference(CASE_ID));

            verify(notificationClient).sendEmail(
                APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
                "FamilyPublicLaw+ctsc@gmail.com",
                expectedCtscNotificationParameters(),
                notificationReference(CASE_ID));
        });
        verifyCafcassOrderNotification();
    }

    @Test
    void shouldSendFailedPaymentNotificationWhenDisplayAmountToPayNotSet() {
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);

        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);
        postSubmittedEvent(callbackRequest);

        checkUntil(() -> {
            verify(notificationClient).sendEmail(
                APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA,
                LOCAL_AUTHORITY_1_INBOX,
                expectedLocalAuthorityNotificationParameters(),
                notificationReference(CASE_ID));

            verify(notificationClient).sendEmail(
                APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
                "FamilyPublicLaw+ctsc@gmail.com",
                expectedCtscNotificationParameters(),
                notificationReference(CASE_ID));
        });
        verifyCafcassOrderNotification();
    }

    @Test
    void shouldSendFailedPaymentNotificationOnHiddenDisplayAmountToPay() {
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
        caseDetails.getData().put(DISPLAY_AMOUNT_TO_PAY, NO.getValue());

        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);
        postSubmittedEvent(callbackRequest);

        checkUntil(() -> {
            verify(notificationClient).sendEmail(
                APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA,
                LOCAL_AUTHORITY_1_INBOX,
                expectedLocalAuthorityNotificationParameters(),
                notificationReference(CASE_ID));

            verify(notificationClient).sendEmail(
                APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
                "FamilyPublicLaw+ctsc@gmail.com",
                expectedCtscNotificationParameters(),
                notificationReference(CASE_ID));
        });
    }

    @Test
    void shouldPopulateResponseWithMarkdown() {
        String caseName = "Names are hard";

        CaseDetails caseDetails = populatedCaseDetails(Map.of("caseName", caseName));
        caseDetails.getData().put("representativeType", RepresentativeType.LOCAL_AUTHORITY);

        CallbackRequest request = buildCallbackRequest(caseDetails, OPEN);

        SubmittedCallbackResponse response = postSubmittedEvent(request);
        String expectedHeader = "# Application sent\n\n## " + caseName;
        String expectedBody = "## What happens next\n\n"
            + "We’ll check your application – we might need to ask you more questions, or send it back to you to amend."
            + "\n\nIf we have no questions, we’ll send your application to the local court gatekeeper.\n\n"
            + "You can contact us at contactFPL@justice.gov.uk.\n\n"
            + "## Help us improve this service\n\n"
            + "Tell us how this service was today on our <a href=\"" + SURVEY_LINK + "\" target=\"_blank\">feedback "
            + "form</a>.";

        assertThat(response).extracting("confirmationHeader", "confirmationBody")
            .containsExactly(expectedHeader, expectedBody);
        verifyCafcassOrderNotification();
    }

    @Nested
    class CaseResubmission {

        final State state = RETURNED;

        @Test
        void shouldNotifyAdminAndCafcassWhenCaseIsResubmitted() {
            CaseDetails caseDetails = enableSendToCtscOnCaseDetails(NO);
            caseDetails.getData().put(DISPLAY_AMOUNT_TO_PAY, YES.getValue());

            CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, state);
            postSubmittedEvent(callbackRequest);

            checkUntil(() -> resubmissionNotificationsSent(COURT_1.getEmail()));
            checkThat(this::paymentNotTakenAndNoMoreEmailsSent);
        }

        @Test
        void shouldNotifyCtscAndCafcassWhenCaseIsResubmitted() {
            CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
            caseDetails.getData().put(DISPLAY_AMOUNT_TO_PAY, YES.getValue());

            CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, state);
            postSubmittedEvent(callbackRequest);

            checkUntil(() -> resubmissionNotificationsSent(CTSC_EMAIL));
            checkThat(this::paymentNotTakenAndNoMoreEmailsSent);
        }

        private void resubmissionNotificationsSent(String adminEmail) throws Exception {
            verify(notificationClient).sendEmail(
                eq(AMENDED_APPLICATION_RETURNED_ADMIN_TEMPLATE),
                eq(adminEmail),
                anyMap(),
                eq(notificationReference(CASE_ID)));

            verify(notificationClient).sendEmail(
                eq(AMENDED_APPLICATION_RETURNED_CAFCASS_TEMPLATE),
                eq(CAFCASS_EMAIL),
                anyMap(),
                eq(notificationReference(CASE_ID)));
        }

        private void paymentNotTakenAndNoMoreEmailsSent() {
            verifyNoMoreInteractions(notificationClient);
            verifyNoMoreInteractions(paymentService);
        }

        @AfterEach
        void resetMocks() {
            reset(notificationClient);
        }
    }

    private Map<String, Object> expectedCtscNotificationParameters() {
        return Map.of("applicationType", "C110a",
            "caseUrl", caseUrl(CASE_ID),
            "applicant", LOCAL_AUTHORITY_1_NAME);
    }

    private Map<String, Object> expectedLocalAuthorityNotificationParameters() {
        return Map.of("applicationType", "C110a",
            "caseUrl", caseUrl(CASE_ID));
    }

    private CaseDetails enableSendToCtscOnCaseDetails(YesNo enableCtsc) {
        return CaseDetails.builder()
            .id(CASE_ID)
            .data(new HashMap<>(Map.of(
                "submittedForm", DocumentReference.builder().binaryUrl("/testUrl").build(),
                RETURN_APPLICATION, ReturnApplication.builder()
                    .note("Some note")
                    .reason(List.of(INCOMPLETE))
                    .document(TestDataHelper.testDocumentReference())
                    .build(),
                "orders", Orders.builder()
                    .emergencyProtectionOrderDirections(List.of(CONTACT_WITH_NAMED_PERSON))
                    .orderType(List.of(EMERGENCY_PROTECTION_ORDER))
                    .build(),
                "caseLocalAuthority", LOCAL_AUTHORITY_1_CODE,
                "caseLocalAuthorityName", LOCAL_AUTHORITY_1_NAME,
                "sendToCtsc", enableCtsc.getValue(),
                "dateSubmitted", LocalDate.of(2020, 1, 1),
                "representativeType", RepresentativeType.LOCAL_AUTHORITY,
                "localAuthorities", wrapElementsWithUUIDs(LocalAuthority.builder()
                    .id(LOCAL_AUTHORITY_1_CODE)
                    .name(LOCAL_AUTHORITY_1_NAME)
                    .designated(YES.getValue())
                    .email(LOCAL_AUTHORITY_1_INBOX)
                    .build())
            ))).build();
    }

    private SubmitCaseHmctsTemplate getExpectedHmctsParameters(boolean completed, String laCourt, String laName) {
        SubmitCaseHmctsTemplate submitCaseHmctsTemplate =
                SubmitCaseHmctsTemplate.builder()
                    .localAuthority(laName)
                .build();

        if (completed) {
            getCompleteParameters(submitCaseHmctsTemplate);
        } else {
            getIncompleteParameters(submitCaseHmctsTemplate);
        }

        submitCaseHmctsTemplate.setCourt(laCourt);
        return submitCaseHmctsTemplate;
    }

    private Map<String, Object> getExpectedCafcassParameters(boolean completed, String cafcassCourt, String laName) {
        SubmitCaseCafcassTemplate submitCaseCafcassTemplate =
                SubmitCaseCafcassTemplate.builder()
                    .localAuthority(laName)
                .build();

        String fileContent = new String(Base64.encodeBase64(DOCUMENT_CONTENT), ISO_8859_1);
        JSONObject jsonFileObject = new JSONObject()
            .put("file", fileContent)
            .put("is_csv", false);

        if (completed) {
            getCompleteParameters(submitCaseCafcassTemplate);
        } else {
            getIncompleteParameters(submitCaseCafcassTemplate);
        }

        submitCaseCafcassTemplate.setCafcass(cafcassCourt);
        submitCaseCafcassTemplate.setDocumentLink(jsonFileObject.toMap());
        return toMap(submitCaseCafcassTemplate);
    }

    private Map<String, Object> getExpectedRegisteredSolicitorParameters() {
        RegisteredRepresentativeSolicitorTemplate template = RegisteredRepresentativeSolicitorTemplate.builder()
            .salutation("Dear First Representative")
            .clientFullName(String.format("%s %s", RESPONDENT_FIRST_NAME, RESPONDENT_LAST_NAME))
            .localAuthority(LOCAL_AUTHORITY_1_NAME)
            .ccdNumber(CASE_ID.toString())
            .caseName(CASE_NAME)
            .manageOrgLink("https://manage-org.platform.hmcts.net")
            .childLastName(EMPTY)
            .build();

        return toMap(template);
    }

    private <T extends SharedNotifyTemplate> T getCompleteParameters(T template) {
        setSharedTemplateParameters(template);

        template.setTimeFramePresent(YES.getValue());
        template.setTimeFrameValue("same day");
        template.setUrgentHearing(YES.getValue());
        template.setNonUrgentHearing(NO.getValue());
        template.setFirstRespondentName("Smith");
        template.setChildLastName("Reeves");

        return template;
    }

    private <T extends SharedNotifyTemplate> T getIncompleteParameters(T template) {
        setSharedTemplateParameters(template);

        template.setTimeFramePresent(NO.getValue());
        template.setTimeFrameValue("Other");
        template.setUrgentHearing(NO.getValue());
        template.setNonUrgentHearing(YES.getValue());
        template.setFirstRespondentName("");

        return template;
    }

    private <T extends SharedNotifyTemplate> void setSharedTemplateParameters(T template) {
        template.setReference(CASE_ID.toString());
        template.setCaseUrl(caseUrl(CASE_ID));
        template.setDataPresent(YES.getValue());
        template.setFullStop(NO.getValue());
        template.setOrdersAndDirections(List.of("Emergency protection order", "Contact with any named person"));
        template.setDocumentLink("http://fake-url/testUrl");
        template.setChildLastName("");
    }

    private CallbackRequest buildCallbackRequest(CaseDetails caseDetails, State stateBefore) {
        return CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(CaseDetails.builder()
                .state(stateBefore.getValue())
                .data(Map.of())
                .build())
            .build();
    }

    private void verifyTaskListUpdated() {
        verify(coreCaseDataService).performPostSubmitCallback(
            eq(CASE_ID),
            eq("internal-update-task-list"),
            any());
    }

    private void verifyEmailSentToTranslation() {
        checkUntil(() -> verify(emailService, times(1)).sendEmail(eq("sender@example.com"), any()));
    }

    private void verifyNoMoreNotificationsSentToTranslationTeam() {
        checkUntil(() -> verify(emailService, never()).sendEmail(eq("sender@example.com"), any()));
        verifyCafcassOrderNotification();
    }


    private void verifyCafcassOrderNotification() {
        checkUntil(() -> verify(cafcassNotificationService).sendEmail(
            isA(CaseData.class), any(), cafcassRequestEmailContentProviderArgumentCaptor.capture(), any()
        ));
        assertThat(cafcassRequestEmailContentProviderArgumentCaptor.getValue())
            .isEqualTo(CafcassRequestEmailContentProvider.NEW_APPLICATION);
    }
}
