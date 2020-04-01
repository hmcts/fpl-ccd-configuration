package uk.gov.hmcts.reform.fpl.service.payment;

import feign.FeignException;
import feign.Request;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fnp.client.PaymentApi;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
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
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang.StringUtils.EMPTY;
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

        @ParameterizedTest
        @ValueSource(strings = {"customerReference"})
        void shouldMakeCorrectPaymentForC2WithNotice(final String customerReference) {
            CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA")
                .c2DocumentBundle(List.of(element(C2DocumentBundle.builder()
                    .type(WITH_NOTICE)
                    .pbaNumber("PBA123")
                    .clientCode("clientCode")
                    .fileReference(customerReference)
                    .build())))
                .build();
            CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
                .customerReference(customerReference)
                .amount(feeForC2WithNotice.getCalculatedAmount())
                .fees(List.of(feeForC2WithNotice))
                .build();

            paymentService.makePaymentForC2(CASE_ID, caseData);

            verify(paymentApi).createCreditAccountPayment(AUTH_TOKEN, SERVICE_AUTH_TOKEN, expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName("LA");
            verify(feeService).getFeesDataForC2(WITH_NOTICE);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldMakeCorrectPaymentForC2WithNoticeWhenCustomerReferenceIsNullOrEmpty(
            final String customerReference) {
            CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA")
                .c2DocumentBundle(List.of(element(C2DocumentBundle.builder()
                    .type(WITH_NOTICE)
                    .pbaNumber("PBA123")
                    .clientCode("clientCode")
                    .fileReference(customerReference)
                    .build())))
                .build();
            CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
                .customerReference("Not provided")
                .amount(feeForC2WithNotice.getCalculatedAmount())
                .fees(List.of(feeForC2WithNotice))
                .build();

            paymentService.makePaymentForC2(CASE_ID, caseData);

            verify(paymentApi).createCreditAccountPayment(AUTH_TOKEN, SERVICE_AUTH_TOKEN, expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName("LA");
            verify(feeService).getFeesDataForC2(WITH_NOTICE);
        }

        @ParameterizedTest
        @ValueSource(strings = {"customerReference"})
        void shouldMakeCorrectPaymentForC2WithoutNotice(final String customerReference) {
            CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA")
                .c2DocumentBundle(List.of(element(C2DocumentBundle.builder()
                    .type(WITHOUT_NOTICE)
                    .pbaNumber("PBA123")
                    .clientCode("clientCode")
                    .fileReference(customerReference)
                    .build())))
                .build();
            CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
                .customerReference(customerReference)
                .amount(feeForC2WithoutNotice.getCalculatedAmount())
                .fees(List.of(feeForC2WithoutNotice))
                .build();

            paymentService.makePaymentForC2(CASE_ID, caseData);

            verify(paymentApi).createCreditAccountPayment(AUTH_TOKEN, SERVICE_AUTH_TOKEN, expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName("LA");
            verify(feeService).getFeesDataForC2(WITHOUT_NOTICE);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldMakeCorrectPaymentForC2WithoutNoticeWhenCustomerReferenceIsNullOrEmpty(
            final String customerReference) {
            CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA")
                .c2DocumentBundle(List.of(element(C2DocumentBundle.builder()
                    .type(WITHOUT_NOTICE)
                    .pbaNumber("PBA123")
                    .clientCode("clientCode")
                    .fileReference(customerReference)
                    .build())))
                .build();
            CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
                .customerReference("Not provided")
                .amount(feeForC2WithoutNotice.getCalculatedAmount())
                .fees(List.of(feeForC2WithoutNotice))
                .build();

            paymentService.makePaymentForC2(CASE_ID, caseData);

            verify(paymentApi).createCreditAccountPayment(AUTH_TOKEN, SERVICE_AUTH_TOKEN, expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName("LA");
            verify(feeService).getFeesDataForC2(WITHOUT_NOTICE);
        }

        @Test
        void shouldReturnPaymentsApiExceptionOnFeignException() {
            String responseBodyContent = "Response message";
            when(paymentApi.createCreditAccountPayment(any(), any(), any())).thenThrow(
                new FeignException.UnprocessableEntity("",
                    Request.create(GET, EMPTY, Map.of(), new byte[] {}, UTF_8),
                    responseBodyContent.getBytes()));
            CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA")
                .c2DocumentBundle(List.of(element(C2DocumentBundle.builder()
                    .type(WITHOUT_NOTICE)
                    .pbaNumber("PBA123")
                    .clientCode("clientCode")
                    .fileReference("customerReference")
                    .build())))
                .build();

            AssertionsForClassTypes.assertThatThrownBy(() -> paymentService.makePaymentForC2(CASE_ID, caseData))
                .isInstanceOf(PaymentsApiException.class)
                .hasMessage(responseBodyContent);
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
                        .customerReference("customerReference")
                        .build())
                    .build())))
                .orders(orders)
                .build();

            CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
                .customerReference("customerReference")
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
            .description("Payment for case: " + CASE_ID)
            .organisationName("Example Local Authority")
            .service(FPL)
            .siteId("TEST_SITE_ID");
    }
}
