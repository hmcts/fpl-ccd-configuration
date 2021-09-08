package uk.gov.hmcts.reform.fpl.controllers.placement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fnp.client.FeesRegisterApi;
import uk.gov.hmcts.reform.fnp.client.PaymentApi;
import uk.gov.hmcts.reform.fnp.model.payment.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.fnp.model.payment.FeeDto;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.PlacementController;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fnp.model.payment.enums.Currency.GBP;
import static uk.gov.hmcts.reform.fnp.model.payment.enums.Service.FPL;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_UPDATE_EVENT;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_CTSC_EMAIL;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_APPLICANT;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.feeResponse;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.feignException;

@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementSubmittedControllerTest extends AbstractCallbackTest {

    private static final Long CASE_ID = 12345L;
    private static final BigDecimal FEE = BigDecimal.valueOf(455.5);
    private static final String PBA_NUMBER = "PBA1234567";
    private static final String EVENT_TOKEN = randomAlphanumeric(10);

    @MockBean
    private PaymentApi paymentApi;

    @MockBean
    private FeesRegisterApi feesRegisterApi;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private NotificationClient notificationClient;

    PlacementSubmittedControllerTest() {
        super("placement");
    }

    @BeforeEach
    void init() {

        when(feesRegisterApi.findFee("default", "miscellaneous", "family", "family court", "Placement", "adoption"))
            .thenReturn(feeResponse(FEE.doubleValue()));

        when(coreCaseDataApi.startEventForCaseWorker(USER_AUTH_TOKEN,
            SERVICE_AUTH_TOKEN,
            SYS_USER_ID,
            JURISDICTION,
            CASE_TYPE, CASE_ID.toString(),
            CASE_UPDATE_EVENT))
            .thenReturn(StartEventResponse.builder().eventId(CASE_UPDATE_EVENT).token(EVENT_TOKEN).build());

        givenSystemUser();
        givenFplService();
    }

    @Test
    void shouldMakePaymentForPlacement() {

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthorityName(LOCAL_AUTHORITY_1_NAME)
            .placementEventData(PlacementEventData.builder()
                .placementPaymentRequired(YesNo.YES)
                .placementPayment(PBAPayment.builder()
                    .pbaNumber(PBA_NUMBER)
                    .build())
                .build())
            .build();

        when(paymentApi.createCreditAccountPayment(any(), any(), any()))
            .thenReturn(CreditAccountPaymentRequest.builder().build());

        postSubmittedEvent(caseData);

        final CreditAccountPaymentRequest expectedPaymentRequest = expectedCreditAccountPaymentRequest();
        final CaseDataContent expectedCaseUpdateRequest = expectedCaseDataContent();

        verify(paymentApi).createCreditAccountPayment(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, expectedPaymentRequest);

        verify(coreCaseDataApi).submitEventForCaseWorker(
            USER_AUTH_TOKEN,
            SERVICE_AUTH_TOKEN,
            SYS_USER_ID,
            JURISDICTION,
            CASE_TYPE,
            CASE_ID.toString(),
            true,
            expectedCaseUpdateRequest);

        verifyNoInteractions(notificationClient);
    }

    @Test
    void shouldNotifyCourtAndApplicantAboutPaymentFailure() throws NotificationClientException {

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthorityName(LOCAL_AUTHORITY_1_NAME)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .placementEventData(PlacementEventData.builder()
                .placementPaymentRequired(YesNo.YES)
                .placementPayment(PBAPayment.builder()
                    .pbaNumber(PBA_NUMBER)
                    .build())
                .build())
            .build();

        when(paymentApi.createCreditAccountPayment(any(), any(), any())).thenThrow(feignException(403));

        postSubmittedEvent(caseData);

        final CreditAccountPaymentRequest expectedPaymentRequest = expectedCreditAccountPaymentRequest();
        final CaseDataContent expectedCaseUpdateRequest = expectedCaseDataContent();

        verify(paymentApi).createCreditAccountPayment(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, expectedPaymentRequest);

        verify(coreCaseDataApi).submitEventForCaseWorker(
            USER_AUTH_TOKEN,
            SERVICE_AUTH_TOKEN,
            SYS_USER_ID,
            JURISDICTION,
            CASE_TYPE,
            CASE_ID.toString(),
            true,
            expectedCaseUpdateRequest);

        verify(notificationClient).sendEmail(
            INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
            DEFAULT_CTSC_EMAIL,
            Map.of("caseUrl", caseUrl(caseData.getId(), "Placement"),
                "applicationType", "A50 - Application for a Placement Order",
                "applicant", LOCAL_AUTHORITY_1_NAME),
            notificationReference(CASE_ID));

        verify(notificationClient).sendEmail(
            INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_APPLICANT,
            LOCAL_AUTHORITY_1_INBOX,
            Map.of("caseUrl", caseUrl(caseData.getId(), "Placement"),
                "applicationType", "A50 - Application for a Placement Order"),
            notificationReference(CASE_ID));
    }

    @Test
    void shouldNotMakePaymentForPlacementIfNotRequired() {

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .placementEventData(PlacementEventData.builder()
                .placementPaymentRequired(NO)
                .build())
            .build();

        postSubmittedEvent(caseData);

        verifyNoInteractions(feesRegisterApi, paymentApi, coreCaseDataApi, notificationClient);
    }

    private CreditAccountPaymentRequest expectedCreditAccountPaymentRequest() {
        return CreditAccountPaymentRequest.builder()
            .accountNumber(PBA_NUMBER)
            .amount(FEE)
            .caseReference("Not provided")
            .currency(GBP)
            .customerReference("Not provided")
            .description(String.format("Payment for case: %s", CASE_ID))
            .service(FPL)
            .siteId("SITE_ID")
            .fees(List.of(FeeDto.builder()
                .calculatedAmount(FEE)
                .build()))
            .ccdCaseNumber(CASE_ID.toString())
            .organisationName(LOCAL_AUTHORITY_1_NAME)
            .build();
    }

    private CaseDataContent expectedCaseDataContent() {
        final Map<String, Object> expectedCaseChanges = new HashMap<>();
        expectedCaseChanges.put("placementLastPaymentTime", now());
        expectedCaseChanges.put("placementPaymentRequired", null);
        expectedCaseChanges.put("placementPayment", null);

        return CaseDataContent.builder()
            .event(Event.builder()
                .id(CASE_UPDATE_EVENT)
                .build())
            .data(expectedCaseChanges)
            .eventToken(EVENT_TOKEN)
            .ignoreWarning(false)
            .build();
    }
}
