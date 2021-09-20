package uk.gov.hmcts.reform.fpl.service.payment;

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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fnp.model.payment.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.fnp.model.payment.FeeDto;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.C2ApplicationType;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import java.math.BigDecimal;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fnp.model.payment.enums.Currency.GBP;
import static uk.gov.hmcts.reform.fnp.model.payment.enums.Service.FPL;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.service.payment.PaymentService.BLANK_PARAMETER_VALUE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PaymentService.class})
@TestPropertySource(properties = {"payment.site_id=TEST_SITE_ID"})
class PaymentServiceTest {

    private static final Long CASE_ID = 1L;

    @MockBean
    private FeeService feeService;

    @MockBean
    private PaymentClient paymentClient;

    @MockBean
    private LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private PaymentService paymentService;

    @BeforeEach
    void setup() {
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

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName("LA");
            verify(feeService).getFeesDataForC2(WITH_NOTICE);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldMakeCorrectPaymentForC2WithNoticeWhenCustomerReferenceIsInvalid(final String customerReference) {
            String clientCode = "clientCode";
            CaseData caseData = buildCaseData(clientCode, customerReference, WITH_NOTICE);

            CreditAccountPaymentRequest expectedPaymentRequest = buildCreditAccountPaymentRequest(clientCode,
                customerReference,
                feeForC2WithNotice);

            paymentService.makePaymentForC2(CASE_ID, caseData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName("LA");
            verify(feeService).getFeesDataForC2(WITH_NOTICE);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldMakeCorrectPaymentForC2WithNoticeWhenCaseReferenceIsInvalid(final String clientCode) {
            String customerReference = "customerReference";
            CaseData caseData = buildCaseData(clientCode, customerReference, WITH_NOTICE);

            CreditAccountPaymentRequest expectedPaymentRequest = buildCreditAccountPaymentRequest(clientCode,
                customerReference,
                feeForC2WithNotice);

            paymentService.makePaymentForC2(CASE_ID, caseData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
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

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName("LA");
            verify(feeService).getFeesDataForC2(WITHOUT_NOTICE);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldMakeCorrectPaymentForC2WithoutNoticeWhenCustomerReferenceIsInvalid(final String customerReference) {
            String clientCode = "clientCode";
            CaseData caseData = buildCaseData(clientCode, customerReference, WITHOUT_NOTICE);

            CreditAccountPaymentRequest expectedPaymentRequest = buildCreditAccountPaymentRequest(clientCode,
                customerReference,
                feeForC2WithoutNotice);

            paymentService.makePaymentForC2(CASE_ID, caseData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName("LA");
            verify(feeService).getFeesDataForC2(WITHOUT_NOTICE);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldMakeCorrectPaymentForC2WithoutNoticeWhenCaseReferenceIsInvalid(final String clientCode) {
            String customerReference = "customerReference";
            CaseData caseData = buildCaseData(clientCode, customerReference, WITHOUT_NOTICE);

            CreditAccountPaymentRequest expectedPaymentRequest = buildCreditAccountPaymentRequest(clientCode,
                customerReference,
                feeForC2WithoutNotice);

            paymentService.makePaymentForC2(CASE_ID, caseData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName("LA");
            verify(feeService).getFeesDataForC2(WITHOUT_NOTICE);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldMakeExpectedPaymentWhenFeeAndPayCaseTypeFeatureToggleIsToggledOnAndOff(final boolean toggleStatus) {
            String customerReference = "customerReference";
            CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA")
                .c2DocumentBundle(List.of(element(C2DocumentBundle.builder()
                    .type(WITH_NOTICE)
                    .pbaNumber("PBA123")
                    .clientCode("clientCode")
                    .fileReference(customerReference)
                    .build())))
                .build();

            when(featureToggleService.isFeeAndPayCaseTypeEnabled()).thenReturn(toggleStatus);

            CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
                .customerReference(customerReference)
                .amount(feeForC2WithNotice.getCalculatedAmount())
                .fees(List.of(feeForC2WithNotice))
                .build();

            paymentService.makePaymentForC2(CASE_ID, caseData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName("LA");
            verify(feeService).getFeesDataForC2(WITH_NOTICE);
        }

        private FeesData buildFeesData(FeeDto feeDto) {
            return FeesData.builder()
                .totalAmount(feeDto.getCalculatedAmount())
                .fees(List.of(feeDto))
                .build();
        }
    }

    @Nested
    class MakePaymentForAdditionalApplications {
        String testPbaNumber = "PBA123";
        FeeDto feeForAdditionalApplications = FeeDto.builder().calculatedAmount(BigDecimal.TEN).build();
        FeesData feesData = buildFeesData(feeForAdditionalApplications);

        @Test
        void shouldMakeCorrectPaymentForAdditionalApplications() {
            CaseData caseData = buildCaseData("clientCode", "customerReference");

            CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
                .customerReference("customerReference")
                .amount(feeForAdditionalApplications.getCalculatedAmount())
                .fees(List.of(feeForAdditionalApplications))
                .build();

            paymentService.makePaymentForAdditionalApplications(CASE_ID, caseData, feesData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName("LA");
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldMakeCorrectPaymentForAdditionalApplicationsWhenCustomerReferenceIsInvalid(
            final String customerReference) {
            String clientCode = "clientCode";
            CaseData caseData = buildCaseData(clientCode, customerReference);

            CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
                .customerReference(BLANK_PARAMETER_VALUE)
                .amount(feeForAdditionalApplications.getCalculatedAmount())
                .fees(List.of(FeeDto.builder().calculatedAmount(BigDecimal.TEN).build()))
                .build();

            paymentService.makePaymentForAdditionalApplications(CASE_ID, caseData, feesData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName("LA");
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldMakeCorrectPaymentForAdditionalApplicationsWhenCaseReferenceIsInvalid(final String clientCode) {
            String customerReference = "customerReference";
            CaseData caseData = buildCaseData(clientCode, customerReference);

            CreditAccountPaymentRequest expectedPaymentRequest = buildCreditAccountPaymentRequest(clientCode,
                customerReference,
                feeForAdditionalApplications);

            paymentService.makePaymentForAdditionalApplications(CASE_ID, caseData, feesData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName("LA");
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldMakeExpectedPaymentWhenFeeAndPayCaseTypeFeatureToggleIsToggledOnAndOff(final boolean toggleStatus) {
            String customerReference = "customerReference";
            CaseData caseData = buildCaseData("clientCode", "customerReference");

            when(featureToggleService.isFeeAndPayCaseTypeEnabled()).thenReturn(toggleStatus);

            CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
                .customerReference(customerReference)
                .amount(feeForAdditionalApplications.getCalculatedAmount())
                .fees(List.of(feeForAdditionalApplications))
                .build();

            paymentService.makePaymentForAdditionalApplications(CASE_ID, caseData, feesData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName("LA");
        }

        private CaseData buildCaseData(String clientCode, String customerReference) {
            return CaseData.builder()
                .caseLocalAuthority("LA")
                .additionalApplicationsBundle(List.of(
                    element(AdditionalApplicationsBundle.builder()
                        .pbaPayment(PBAPayment.builder()
                            .clientCode(clientCode)
                            .fileReference(customerReference)
                            .pbaNumber(testPbaNumber)
                            .build()).build()))).build();
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
        void shouldTakePaymentContextDetailsFromLocalAuthorityDetails() {
            when(feeService.getFeesDataForOrders(orders)).thenReturn(FeesData.builder()
                .totalAmount(BigDecimal.TEN)
                .fees(List.of(careOrderFee, supervisionOrderFee))
                .build());

            final LocalAuthority localAuthority = LocalAuthority.builder()
                .designated("Yes")
                .pbaNumber("PBA1234567")
                .customerReference("localAuthorityReference")
                .clientCode("localAuthorityCode")
                .build();

            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority("LA")
                .localAuthorities(wrapElements(localAuthority))
                .applicants(List.of(element(Applicant.builder()
                    .party(ApplicantParty.builder()
                        .pbaNumber("PBA123")
                        .clientCode("clientCode")
                        .customerReference("customerReference")
                        .build())
                    .build())))
                .orders(orders)
                .build();

            final CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
                .customerReference("localAuthorityReference")
                .caseReference("localAuthorityCode")
                .accountNumber("PBA1234567")
                .amount(BigDecimal.TEN)
                .fees(List.of(careOrderFee, supervisionOrderFee))
                .build();

            paymentService.makePaymentForCaseOrders(caseData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName("LA");
            verify(feeService).getFeesDataForOrders(orders);
        }

        @Test
        void shouldTakePaymentContextDetailsFromLegacyApplicantDetailsWhenNoLocalAuthority() {
            when(feeService.getFeesDataForOrders(orders)).thenReturn(FeesData.builder()
                .totalAmount(BigDecimal.TEN)
                .fees(List.of(careOrderFee, supervisionOrderFee))
                .build());

            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
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

            paymentService.makePaymentForCaseOrders(caseData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName("LA");
            verify(feeService).getFeesDataForOrders(orders);
        }

        @Test
        void shouldNotMakePaymentForZeroTotalAmount() {
            when(feeService.getFeesDataForOrders(orders)).thenReturn(FeesData.builder()
                .totalAmount(BigDecimal.ZERO)
                .fees(List.of())
                .build());
            CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .orders(orders).build();

            paymentService.makePaymentForCaseOrders(caseData);

            verify(paymentClient, never()).callPaymentsApi(any());
            verify(localAuthorityNameLookupConfiguration, never()).getLocalAuthorityName(any());
            verify(feeService).getFeesDataForOrders(orders);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldMakeCorrectPaymentForC110AWhenCustomerReferenceIsInvalid(final String customerReference) {
            String clientCode = "clientCode";

            when(feeService.getFeesDataForOrders(orders)).thenReturn(FeesData.builder()
                .totalAmount(BigDecimal.TEN)
                .fees(List.of(careOrderFee, supervisionOrderFee))
                .build());

            CaseData caseData = buildCaseDataForC110Application(clientCode, customerReference, orders);

            CreditAccountPaymentRequest expectedPaymentRequest = buildCreditAccountPaymentRequestForC110Application(
                clientCode,
                customerReference);

            paymentService.makePaymentForCaseOrders(caseData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName("LA");
            verify(feeService).getFeesDataForOrders(orders);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldMakeCorrectPaymentForC110AWhenCaseReferenceIsInvalid(final String clientCode) {
            String customerReference = "customerReference";

            when(feeService.getFeesDataForOrders(orders)).thenReturn(FeesData.builder()
                .totalAmount(BigDecimal.TEN)
                .fees(List.of(careOrderFee, supervisionOrderFee))
                .build());

            CaseData caseData = buildCaseDataForC110Application(clientCode, customerReference, orders);

            CreditAccountPaymentRequest expectedPaymentRequest = buildCreditAccountPaymentRequestForC110Application(
                clientCode,
                customerReference);

            paymentService.makePaymentForCaseOrders(caseData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName("LA");
            verify(feeService).getFeesDataForOrders(orders);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldMakeExpectedPaymentWhenFeeAndPayCaseTypeFeatureToggleIsToggledOnAndOff(final boolean toggleStatus) {
            when(feeService.getFeesDataForOrders(orders)).thenReturn(FeesData.builder()
                .totalAmount(BigDecimal.TEN)
                .fees(List.of(careOrderFee, supervisionOrderFee))
                .build());
            CaseData caseData = CaseData.builder()
                .id(CASE_ID)
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

            when(featureToggleService.isFeeAndPayCaseTypeEnabled()).thenReturn(toggleStatus);

            CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
                .customerReference("customerReference")
                .amount(BigDecimal.TEN)
                .fees(List.of(careOrderFee, supervisionOrderFee))
                .build();

            paymentService.makePaymentForCaseOrders(caseData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName("LA");
            verify(feeService).getFeesDataForOrders(orders);
        }
    }

    @AfterEach
    void resetInvocations() {
        reset(localAuthorityNameLookupConfiguration);
        reset(feeService);
        reset(paymentClient);
    }

    private CreditAccountPaymentRequest.CreditAccountPaymentRequestBuilder creditAccountPaymentRequestBuilder() {
        CreditAccountPaymentRequest.CreditAccountPaymentRequestBuilder builder = CreditAccountPaymentRequest.builder()
            .accountNumber("PBA123")
            .currency(GBP)
            .service(FPL)
            .ccdCaseNumber(String.valueOf(CASE_ID))
            .description("Payment for case: " + CASE_ID)
            .organisationName("Example Local Authority");

        if (featureToggleService.isFeeAndPayCaseTypeEnabled()) {
            builder.caseType(CASE_TYPE);
        } else {
            builder.siteId("TEST_SITE_ID");
        }

        return builder;
    }

    private CreditAccountPaymentRequest.CreditAccountPaymentRequestBuilder testCreditAccountPaymentRequestBuilder() {
        return creditAccountPaymentRequestBuilder().caseReference("clientCode");
    }

    private CreditAccountPaymentRequest buildCreditAccountPaymentRequest(String caseReference,
                                                                         String customerReference,
                                                                         FeeDto feeDto) {
        return creditAccountPaymentRequestBuilder()
            .caseReference(defaultIfBlank(caseReference, BLANK_PARAMETER_VALUE))
            .customerReference(defaultIfBlank(customerReference, BLANK_PARAMETER_VALUE))
            .amount(feeDto.getCalculatedAmount())
            .fees(List.of(feeDto))
            .build();
    }

    private CreditAccountPaymentRequest buildCreditAccountPaymentRequestForC110Application(String caseReference,
                                                                                           String customerReference) {
        return creditAccountPaymentRequestBuilder()
            .caseReference(defaultIfBlank(caseReference, BLANK_PARAMETER_VALUE))
            .customerReference(defaultIfBlank(customerReference, BLANK_PARAMETER_VALUE))
            .amount(BigDecimal.TEN)
            .fees(List.of(
                FeeDto.builder().calculatedAmount(BigDecimal.ONE).build(),
                FeeDto.builder().calculatedAmount(BigDecimal.TEN).build()))
            .build();
    }

    private CaseData buildCaseData(String clientCode, String customerReference, C2ApplicationType type) {
        return CaseData.builder()
            .caseLocalAuthority("LA")
            .c2DocumentBundle(List.of(element(C2DocumentBundle.builder()
                .type(type)
                .pbaNumber("PBA123")
                .clientCode(clientCode)
                .fileReference(customerReference)
                .build())))
            .build();
    }

    private CaseData buildCaseDataForC110Application(String clientCode, String customerReference, Orders orders) {
        return CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority("LA")
            .localAuthorities(wrapElements(LocalAuthority.builder()
                .designated("Yes")
                .pbaNumber("PBA123")
                .clientCode(clientCode)
                .customerReference(customerReference)
                .build()))
            .orders(orders)
            .build();
    }
}
