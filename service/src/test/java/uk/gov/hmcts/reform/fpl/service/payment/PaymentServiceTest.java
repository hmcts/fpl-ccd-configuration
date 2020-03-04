package uk.gov.hmcts.reform.fpl.service.payment;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fnp.client.PaymentApi;
import uk.gov.hmcts.reform.fnp.model.payment.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.fnp.model.payment.FeeDto;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.request.RequestData;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fnp.model.payment.enums.Currency.GBP;
import static uk.gov.hmcts.reform.fnp.model.payment.enums.Service.FPL;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PaymentService.class})
class PaymentServiceTest {

    private static final String SERVICE_AUTH_TOKEN = "servicetoken";
    private static final String AUTH_TOKEN = "token";
    private static final Long CASE_ID = 1L;

    @MockBean
    private FeeService feeService;

    @MockBean
    private PaymentApi paymentApi;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private RequestData requestData;

    @MockBean
    private LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;

    @Autowired
    private PaymentService paymentService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(paymentService, "siteId", "TEST_SITE_ID", String.class);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(requestData.authorisation()).thenReturn(AUTH_TOKEN);
        when(localAuthorityNameLookupConfiguration.getLocalAuthorityName(any())).thenReturn("Example Local Authority");
    }

    @Nested
    class MakePaymentForC2 {

        FeeDto feeForC2WithNotice = FeeDto.builder().calculatedAmount(BigDecimal.TEN).build();
        FeeDto feeForC2WithoutNotice = FeeDto.builder().calculatedAmount(BigDecimal.ONE).build();

        @BeforeEach
        void setup() {
            when(feeService.getFeesDataForC2(WITH_NOTICE)).thenReturn(buildFeesData(feeForC2WithNotice));
            when(feeService.getFeesDataForC2(WITHOUT_NOTICE)).thenReturn(buildFeesData(feeForC2WithoutNotice));
        }

        @Test
        void shouldMakeCorrectPaymentForC2WithNotice() {
            CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA")
                .c2DocumentBundle(List.of(element(C2DocumentBundle.builder()
                    .type(WITH_NOTICE)
                    .pbaNumber("PBA123")
                    .clientCode("clientCode")
                    .fileReference("fileReference")
                    .build())))
                .build();
            CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
                .amount(feeForC2WithNotice.getCalculatedAmount())
                .fees(List.of(feeForC2WithNotice))
                .build();

            paymentService.makePaymentForC2(CASE_ID, caseData);

            verify(paymentApi).createCreditAccountPayment(AUTH_TOKEN, SERVICE_AUTH_TOKEN, expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName("LA");
            verify(feeService).getFeesDataForC2(WITH_NOTICE);
        }

        @Test
        void shouldMakeCorrectPaymentForC2WithoutNotice() {
            CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA")
                .c2DocumentBundle(List.of(element(C2DocumentBundle.builder()
                    .type(WITHOUT_NOTICE)
                    .pbaNumber("PBA123")
                    .clientCode("clientCode")
                    .fileReference("fileReference")
                    .build())))
                .build();
            CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
                .amount(feeForC2WithoutNotice.getCalculatedAmount())
                .fees(List.of(feeForC2WithoutNotice))
                .build();

            paymentService.makePaymentForC2(CASE_ID, caseData);

            verify(paymentApi).createCreditAccountPayment(AUTH_TOKEN, SERVICE_AUTH_TOKEN, expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName("LA");
            verify(feeService).getFeesDataForC2(WITHOUT_NOTICE);
        }

        private FeesData buildFeesData(FeeDto feeDto) {
            return FeesData.builder()
                .totalAmount(feeDto.getCalculatedAmount())
                .fees(List.of(feeDto))
                .build();
        }
    }

    @Nested
    class MakePaymentForCase {

        Orders orders = Orders.builder().orderType(List.of(CARE_ORDER, SUPERVISION_ORDER)).build();
        FeeDto careOrderFee = FeeDto.builder().calculatedAmount(BigDecimal.ONE).build();
        FeeDto supervisionOrderFee = FeeDto.builder().calculatedAmount(BigDecimal.TEN).build();

        @Test
        void shouldMakeCorrectPaymentForCaseOrders() {
            when(feeService.getFeesDataForOrders(orders)).thenReturn(FeesData.builder()
                .totalAmount(BigDecimal.TEN)
                .fees(List.of(careOrderFee, supervisionOrderFee))
                .build());
            CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA")
                .applicants(List.of(element(Applicant.builder()
                    .party(ApplicantParty.builder()
                        .pbaNumber("PBA123")
                        .clientCode("clientCode")
                        .customerReference("fileReference")
                        .build())
                    .build())))
                .orders(orders)
                .build();

            CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
                .amount(BigDecimal.TEN)
                .fees(List.of(careOrderFee, supervisionOrderFee))
                .build();

            paymentService.makePaymentForCaseOrders(CASE_ID, caseData);

            verify(paymentApi).createCreditAccountPayment(AUTH_TOKEN, SERVICE_AUTH_TOKEN, expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName("LA");
            verify(feeService).getFeesDataForOrders(orders);
        }

        @Test
        void shouldNotMakePaymentForZeroTotalAmount() {
            when(feeService.getFeesDataForOrders(orders)).thenReturn(FeesData.builder()
                .totalAmount(BigDecimal.ZERO)
                .fees(List.of())
                .build());
            CaseData caseData = CaseData.builder().orders(orders).build();

            paymentService.makePaymentForCaseOrders(CASE_ID, caseData);

            verify(paymentApi, never()).createCreditAccountPayment(any(), any(), any());
            verify(localAuthorityNameLookupConfiguration, never()).getLocalAuthorityName(any());
            verify(feeService).getFeesDataForOrders(orders);
        }
    }

    @AfterEach
    void resetInvocations() {
        reset(localAuthorityNameLookupConfiguration);
        reset(feeService);
        reset(paymentApi);
    }

    private CreditAccountPaymentRequest.CreditAccountPaymentRequestBuilder testCreditAccountPaymentRequestBuilder() {
        return CreditAccountPaymentRequest.builder()
            .accountNumber("PBA123")
            .caseReference("clientCode")
            .ccdCaseNumber(String.valueOf(CASE_ID))
            .currency(GBP)
            .customerReference("fileReference")
            .description("Payment for case: " + CASE_ID)
            .organisationName("Example Local Authority")
            .service(FPL)
            .siteId("TEST_SITE_ID");
    }
}
