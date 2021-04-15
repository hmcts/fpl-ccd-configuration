package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.ReturnApplication;
import uk.gov.hmcts.reform.fpl.model.UnregisteredOrganisation;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.SharedNotifyTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.RespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseCafcassTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseHmctsTemplate;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;
import uk.gov.service.notify.NotificationClient;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_CAFCASS_COURT;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_LA_COURT;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.AMENDED_APPLICATION_RETURNED_ADMIN_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.AMENDED_APPLICATION_RETURNED_CAFCASS_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.OUTSOURCED_CASE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.REGISTERED_RESPONDENT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.UNREGISTERED_RESPONDENT_SOLICICTOR;
import static uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType.CONTACT_WITH_NAMED_PERSON;
import static uk.gov.hmcts.reform.fpl.controllers.ReturnApplicationController.RETURN_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.ReturnedApplicationReasons.INCOMPLETE;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.State.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkThat;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;

@WebMvcTest(CaseSubmissionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseSubmissionControllerSubmittedTest extends AbstractCallbackTest {
    private static final String HMCTS_ADMIN_EMAIL = "admin@family-court.com";
    private static final String CAFCASS_EMAIL = "cafcass@cafcass.com";
    private static final String CTSC_EMAIL = "FamilyPublicLaw+ctsc@gmail.com";
    private static final String LA_EMAIL = "FamilyPublicLaw+PublicLawEmail@gmail.com";
    private static final String SOLICITOR_EMAIL = "solicitor@email.com";
    private static final String SOLICITOR_FIRST_NAME = "John";
    private static final String SOLICITOR_LAST_NAME = "Smith";
    private static final String DISPLAY_AMOUNT_TO_PAY = "displayAmountToPay";
    private static final String SURVEY_LINK = "https://fake.survey.url";
    private static final Long CASE_ID = nextLong();
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;

    @Autowired
    protected ObjectMapper mapper;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    CaseSubmissionControllerSubmittedTest() {
        super("case-submission");
    }

    @BeforeEach
    void init() {
        when(documentDownloadService.downloadDocument(any())).thenReturn(DOCUMENT_CONTENT);
    }

    @Test
    void shouldBuildNotificationTemplatesWithCompleteValues() {
        final Map<String, Object> expectedHmctsParameters = mapper.convertValue(
            getExpectedHmctsParameters(true), new TypeReference<>() {
            });

        final Map<String, Object> completeCafcassParameters = mapper.convertValue(
            getExpectedCafcassParameters(true), new TypeReference<>() {
            });

        CaseDetails caseDetails = populatedCaseDetails(Map.of("id", CASE_ID));
        caseDetails.getData().put(DISPLAY_AMOUNT_TO_PAY, YES.getValue());
        caseDetails.getData().put("submittedForm", DocumentReference.builder().binaryUrl("testUrl").build());

        postSubmittedEvent(buildCallbackRequest(caseDetails, OPEN));

        checkUntil(() -> {
            verify(notificationClient).sendEmail(
                HMCTS_COURT_SUBMISSION_TEMPLATE,
                HMCTS_ADMIN_EMAIL,
                expectedHmctsParameters,
                NOTIFICATION_REFERENCE);

            verify(notificationClient).sendEmail(
                CAFCASS_SUBMISSION_TEMPLATE,
                CAFCASS_EMAIL,
                completeCafcassParameters,
                NOTIFICATION_REFERENCE);
        });

        checkThat(() -> verifyNoMoreInteractions(notificationClient));
        verifyTaskListUpdated(CASE_ID);

        verify(coreCaseDataService).triggerEvent(eq(JURISDICTION), eq(CASE_TYPE), eq(CASE_ID),
            eq("internal-update-case-summary"), anyMap());
    }

    @Test
    void shouldNotifyRegisteredSolicitorsWhenCaseIsSubmitted() {
        CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .respondents1(wrapElements(Respondent.builder()
                .legalRepresentation("Yes")
                .solicitor(RespondentSolicitor.builder()
                    .firstName("First").lastName("Respondent")
                    .email(SOLICITOR_EMAIL)
                    .organisation(Organisation.builder().organisationID("123").build()).build())
                .build()))
            .id(CASE_ID)
            .displayAmountToPay(YES.getValue())
            .submittedForm(DocumentReference.builder().binaryUrl("testUrl").build())
            .build();

        final Map<String, Object> registeredSolicitorParameters = mapper.convertValue(
            getExpectedRegisteredSolicitorParameters(), new TypeReference<>() {
            });

        postSubmittedEvent(buildCallbackRequest(asCaseDetails(caseData), OPEN));

        checkUntil(() ->
            verify(notificationClient).sendEmail(
                REGISTERED_RESPONDENT_SUBMISSION_TEMPLATE,
                SOLICITOR_EMAIL,
                registeredSolicitorParameters,
                NOTIFICATION_REFERENCE));
    }

    @Test
    void shouldUpdateTheCaseManagementSummary() {
        CaseDetails caseDetails = populatedCaseDetails(Map.of("id", CASE_ID));

        postSubmittedEvent(buildCallbackRequest(caseDetails, OPEN));

        verify(coreCaseDataService).triggerEvent(eq(JURISDICTION), eq(CASE_TYPE), eq(CASE_ID),
            eq("internal-update-case-summary"), anyMap());
    }

    @Test
    void shouldBuildNotificationTemplatesWithValuesMissingInCallback() {
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(NO);
        caseDetails.getData().put(DISPLAY_AMOUNT_TO_PAY, YES.getValue());
        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);

        postSubmittedEvent(callbackRequest);

        Map<String, Object> expectedIncompleteHmctsParameters = mapper.convertValue(
            getExpectedHmctsParameters(false), new TypeReference<>() {
            });

        checkUntil(() -> {
            verify(notificationClient).sendEmail(
                HMCTS_COURT_SUBMISSION_TEMPLATE,
                HMCTS_ADMIN_EMAIL,
                expectedIncompleteHmctsParameters,
                NOTIFICATION_REFERENCE);

            verify(notificationClient).sendEmail(
                CAFCASS_SUBMISSION_TEMPLATE,
                CAFCASS_EMAIL,
                getExpectedCafcassParameters(false),
                NOTIFICATION_REFERENCE);
        });

        checkThat(() -> verifyNoMoreInteractions(notificationClient));

    }

    @Test
    void shouldSendNotificationToCtscAdminWhenCtscIsEnabledWithinCaseDetails() {
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);

        postSubmittedEvent(callbackRequest);

        Map<String, Object> expectedIncompleteHmctsParameters = mapper.convertValue(
            getExpectedHmctsParameters(false), new TypeReference<>() {
            });

        checkUntil(() ->
            verify(notificationClient).sendEmail(
                HMCTS_COURT_SUBMISSION_TEMPLATE,
                CTSC_EMAIL,
                expectedIncompleteHmctsParameters,
                NOTIFICATION_REFERENCE
            ));

        checkThat(() ->
            verify(notificationClient, never()).sendEmail(
                HMCTS_COURT_SUBMISSION_TEMPLATE,
                HMCTS_ADMIN_EMAIL,
                expectedIncompleteHmctsParameters,
                NOTIFICATION_REFERENCE
            ));
    }

    @Test
    void shouldNotifyManagedLAWhenCaseCreatedOnBehalfOfLA() {
        CaseData caseData = CaseData.builder()
            .outsourcingPolicy(OrganisationPolicy.builder()
                .organisation(Organisation.builder().organisationName("Third party").build())
                .build())
            .caseLocalAuthority("example")
            .id(CASE_ID)
            .build();

        CallbackRequest callbackRequest = buildCallbackRequest(asCaseDetails(caseData), OPEN);

        postSubmittedEvent(callbackRequest);

        checkUntil(() -> verify(notificationClient).sendEmail(
            eq(OUTSOURCED_CASE_TEMPLATE), eq(LA_EMAIL),
            anyMap(), eq(NOTIFICATION_REFERENCE)));
    }

    @Test
    void shouldNotifyUnregisteredSolicitorWhenUnregisteredOrganisationDetailsProvided() {
        Respondent respondent = Respondent.builder()
            .legalRepresentation(YES.getValue())
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

        postSubmittedEvent(buildCallbackRequest(caseDetails, OPEN));

        String expectedSalutation = String.format("Dear %s %s", SOLICITOR_FIRST_NAME, SOLICITOR_LAST_NAME);

        Map<String, Object> expectedUnregisteredSolicitorParameters = mapper.convertValue(
            RespondentSolicitorTemplate.builder()
                .salutation(expectedSalutation)
                .localAuthority(LOCAL_AUTHORITY_1_NAME)
                .build(),
            new TypeReference<>() {
            });

        checkUntil(() ->
            verify(notificationClient).sendEmail(
                UNREGISTERED_RESPONDENT_SOLICICTOR,
                SOLICITOR_EMAIL,
                expectedUnregisteredSolicitorParameters,
                NOTIFICATION_REFERENCE
            ));
    }

    @Test
    void shouldNotNotifyUnregisteredSolicitorWhenUnregisteredOrganisationDetailsNotProvided() {
        Respondent respondent = Respondent.builder()
            .legalRepresentation(YES.getValue())
            .solicitor(RespondentSolicitor.builder()
                .firstName(SOLICITOR_FIRST_NAME)
                .lastName(SOLICITOR_LAST_NAME)
                .email(SOLICITOR_EMAIL)
                .build()).build();

        CaseDetails caseDetails = populatedCaseDetails(Map.of("id", CASE_ID));
        caseDetails.getData().put("respondents1", wrapElements(respondent));

        postSubmittedEvent(buildCallbackRequest(caseDetails, OPEN));

        String expectedSalutation = String.format("Dear %s %s", SOLICITOR_FIRST_NAME, SOLICITOR_LAST_NAME);

        Map<String, Object> expectedUnregisteredSolicitorParameters = mapper.convertValue(
            RespondentSolicitorTemplate.builder()
                .salutation(expectedSalutation)
                .localAuthority(LOCAL_AUTHORITY_1_NAME)
                .build(),
            new TypeReference<>() {
            });

        checkUntil(() ->
            verify(notificationClient, never()).sendEmail(
                UNREGISTERED_RESPONDENT_SOLICICTOR,
                SOLICITOR_EMAIL,
                expectedUnregisteredSolicitorParameters,
                NOTIFICATION_REFERENCE
            ));
    }

    @Test
    void shouldMakePaymentOfAnOpenCaseWhenAmountToPayWasDisplayed() {
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
        caseDetails.getData().put(DISPLAY_AMOUNT_TO_PAY, YES.getValue());
        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);

        postSubmittedEvent(callbackRequest);

        checkUntil(() -> verify(paymentService).makePaymentForCaseOrders(caseConverter.convert(caseDetails)));
    }

    @Test
    void shouldNotMakePaymentOfAnOpenCaseWhenAmountToPayWasNotDisplayed() {
        CaseDetails caseDetails = enableSendToCtscOnCaseDetails(YES);
        caseDetails.getData().put(DISPLAY_AMOUNT_TO_PAY, NO.getValue());
        CallbackRequest callbackRequest = buildCallbackRequest(caseDetails, OPEN);

        postSubmittedEvent(callbackRequest);

        checkThat(() -> verify(paymentService, never()).makePaymentForCaseOrders(any()));
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
                Map.of("applicationType", "C110a"),
                NOTIFICATION_REFERENCE);

            verify(notificationClient).sendEmail(
                APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
                "FamilyPublicLaw+ctsc@gmail.com",
                expectedCtscNotificationParameters(),
                NOTIFICATION_REFERENCE);
        });
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
                Map.of("applicationType", "C110a"),
                NOTIFICATION_REFERENCE);

            verify(notificationClient).sendEmail(
                APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
                "FamilyPublicLaw+ctsc@gmail.com",
                expectedCtscNotificationParameters(),
                NOTIFICATION_REFERENCE);
        });
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
                Map.of("applicationType", "C110a"),
                NOTIFICATION_REFERENCE);

            verify(notificationClient).sendEmail(
                APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
                "FamilyPublicLaw+ctsc@gmail.com",
                expectedCtscNotificationParameters(),
                NOTIFICATION_REFERENCE);
        });
    }

    @Test
    void shouldPopulateResponseWithMarkdown() {
        String caseName = "Names are hard";
        CallbackRequest request = buildCallbackRequest(populatedCaseDetails(
            Map.of("caseName", caseName)
        ), OPEN);

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

            checkUntil(() -> resubmissionNotificationsSent(HMCTS_ADMIN_EMAIL));
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
                eq(NOTIFICATION_REFERENCE));

            verify(notificationClient).sendEmail(
                eq(AMENDED_APPLICATION_RETURNED_CAFCASS_TEMPLATE),
                eq(CAFCASS_EMAIL),
                anyMap(),
                eq(NOTIFICATION_REFERENCE));
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
            "caseUrl", "http://fake-url/cases/case-details/" + CASE_ID);
    }

    private CaseDetails enableSendToCtscOnCaseDetails(YesNo enableCtsc) {
        return CaseDetails.builder()
            .id(CASE_ID)
            .data(new HashMap<>(Map.of(
                "submittedForm", DocumentReference.builder().binaryUrl("testUrl").build(),
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
                "sendToCtsc", enableCtsc.getValue(),
                "dateSubmitted", LocalDate.of(2020, 1, 1)
            ))).build();
    }

    private SubmitCaseHmctsTemplate getExpectedHmctsParameters(boolean completed) {
        SubmitCaseHmctsTemplate submitCaseHmctsTemplate;

        if (completed) {
            submitCaseHmctsTemplate = getCompleteParameters(SubmitCaseHmctsTemplate.builder().build());
        } else {
            submitCaseHmctsTemplate = getIncompleteParameters(SubmitCaseHmctsTemplate.builder().build());
        }

        submitCaseHmctsTemplate.setCourt(DEFAULT_LA_COURT);
        return submitCaseHmctsTemplate;
    }

    private Map<String, Object> getExpectedCafcassParameters(boolean completed) {
        SubmitCaseCafcassTemplate submitCaseCafcassTemplate;

        String fileContent = new String(Base64.encodeBase64(DOCUMENT_CONTENT), ISO_8859_1);
        JSONObject jsonFileObject = new JSONObject()
            .put("file", fileContent)
            .put("is_csv", false);

        if (completed) {
            submitCaseCafcassTemplate = getCompleteParameters(SubmitCaseCafcassTemplate.builder().build());
        } else {
            submitCaseCafcassTemplate = getIncompleteParameters(SubmitCaseCafcassTemplate.builder().build());
        }

        submitCaseCafcassTemplate.setCafcass(DEFAULT_CAFCASS_COURT);
        submitCaseCafcassTemplate.setDocumentLink(jsonFileObject.toMap());
        return mapper.convertValue(submitCaseCafcassTemplate, new TypeReference<>() {
        });
    }

    private Map<String, Object> getExpectedRegisteredSolicitorParameters() {
        RespondentSolicitorTemplate respondentSolicitorTemplate = RespondentSolicitorTemplate.builder()
            .salutation("Dear First Respondent")
            .localAuthority(LOCAL_AUTHORITY_1_NAME)
            .build();

        return mapper.convertValue(respondentSolicitorTemplate, new TypeReference<>() {
        });
    }

    private <T extends SharedNotifyTemplate> T getCompleteParameters(T template) {
        setSharedTemplateParameters(template);

        template.setTimeFramePresent(YES.getValue());
        template.setTimeFrameValue("same day");
        template.setUrgentHearing(YES.getValue());
        template.setNonUrgentHearing(NO.getValue());
        template.setFirstRespondentName("Smith");

        return template;
    }

    private <T extends SharedNotifyTemplate> T getIncompleteParameters(T template) {
        setSharedTemplateParameters(template);

        template.setTimeFramePresent(NO.getValue());
        template.setTimeFrameValue("");
        template.setUrgentHearing(NO.getValue());
        template.setNonUrgentHearing(NO.getValue());
        template.setFirstRespondentName("");

        return template;
    }

    private <T extends SharedNotifyTemplate> void setSharedTemplateParameters(T template) {
        template.setLocalAuthority(LOCAL_AUTHORITY_1_NAME);
        template.setReference(CASE_ID.toString());
        template.setCaseUrl(String.format("http://fake-url/cases/case-details/%s", CASE_ID));
        template.setDataPresent(YES.getValue());
        template.setFullStop(NO.getValue());
        template.setOrdersAndDirections(List.of("Emergency protection order", "Contact with any named person"));
        template.setDocumentLink("testUrl");
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

    private void verifyTaskListUpdated(Long caseId) {
        verify(coreCaseDataService).triggerEvent(
            eq(JURISDICTION),
            eq(CASE_TYPE),
            eq(caseId),
            eq("internal-update-task-list"),
            anyMap());
    }
}
