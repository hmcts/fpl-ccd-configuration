package uk.gov.hmcts.reform.fpl.service.payment;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
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
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    private static final String PBA_NUMBER = "PBA123";
    private static final String CLIENT_CODE = "clientCode";
    private static final String CUSTOMER_REFERENCE = "customerReference";
    private static final String NOT_PROVIDED = "Not provided";
    private static final String LOCAL_AUTHORITY_CODE = "LA";

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

        @Test
        void shouldMakeCorrectPaymentForC2WithNotice() {
            CaseData caseData = CaseData.builder()
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .c2DocumentBundle(List.of(element(C2DocumentBundle.builder()
                    .type(WITH_NOTICE)
                    .pbaNumber(PBA_NUMBER)
                    .clientCode(CLIENT_CODE)
                    .fileReference(CUSTOMER_REFERENCE)
                    .build())))
                .build();

            CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
                .customerReference(CUSTOMER_REFERENCE)
                .amount(feeForC2WithNotice.getCalculatedAmount())
                .fees(List.of(feeForC2WithNotice))
                .build();

            paymentService.makePaymentForC2(CASE_ID, caseData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName(LOCAL_AUTHORITY_CODE);
            verify(feeService).getFeesDataForC2(WITH_NOTICE);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldMakeCorrectPaymentForC2WithNoticeWhenCustomerReferenceIsInvalid(final String customerReference) {
            CaseData caseData = buildCaseData(CLIENT_CODE, customerReference, WITH_NOTICE);

            CreditAccountPaymentRequest expectedPaymentRequest = buildCreditAccountPaymentRequest(CLIENT_CODE,
                customerReference,
                feeForC2WithNotice);

            paymentService.makePaymentForC2(CASE_ID, caseData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName(LOCAL_AUTHORITY_CODE);
            verify(feeService).getFeesDataForC2(WITH_NOTICE);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldMakeCorrectPaymentForC2WithNoticeWhenCaseReferenceIsInvalid(final String clientCode) {
            CaseData caseData = buildCaseData(clientCode, CUSTOMER_REFERENCE, WITH_NOTICE);

            CreditAccountPaymentRequest expectedPaymentRequest = buildCreditAccountPaymentRequest(clientCode,
                CUSTOMER_REFERENCE,
                feeForC2WithNotice);

            paymentService.makePaymentForC2(CASE_ID, caseData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName(LOCAL_AUTHORITY_CODE);
            verify(feeService).getFeesDataForC2(WITH_NOTICE);
        }

        @Test
        void shouldMakeCorrectPaymentForC2WithoutNotice() {
            CaseData caseData = CaseData.builder()
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .c2DocumentBundle(List.of(element(C2DocumentBundle.builder()
                    .type(WITHOUT_NOTICE)
                    .pbaNumber(PBA_NUMBER)
                    .clientCode(CLIENT_CODE)
                    .fileReference(CUSTOMER_REFERENCE)
                    .build())))
                .build();

            CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
                .customerReference(CUSTOMER_REFERENCE)
                .amount(feeForC2WithoutNotice.getCalculatedAmount())
                .fees(List.of(feeForC2WithoutNotice))
                .build();

            paymentService.makePaymentForC2(CASE_ID, caseData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName(LOCAL_AUTHORITY_CODE);
            verify(feeService).getFeesDataForC2(WITHOUT_NOTICE);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldMakeCorrectPaymentForC2WithoutNoticeWhenCustomerReferenceIsInvalid(final String customerReference) {
            CaseData caseData = buildCaseData(CLIENT_CODE, customerReference, WITHOUT_NOTICE);

            CreditAccountPaymentRequest expectedPaymentRequest = buildCreditAccountPaymentRequest(CLIENT_CODE,
                customerReference,
                feeForC2WithoutNotice);

            paymentService.makePaymentForC2(CASE_ID, caseData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName(LOCAL_AUTHORITY_CODE);
            verify(feeService).getFeesDataForC2(WITHOUT_NOTICE);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldMakeCorrectPaymentForC2WithoutNoticeWhenCaseReferenceIsInvalid(final String clientCode) {
            CaseData caseData = buildCaseData(clientCode, CUSTOMER_REFERENCE, WITHOUT_NOTICE);

            CreditAccountPaymentRequest expectedPaymentRequest = buildCreditAccountPaymentRequest(clientCode,
                CUSTOMER_REFERENCE,
                feeForC2WithoutNotice);

            paymentService.makePaymentForC2(CASE_ID, caseData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName(LOCAL_AUTHORITY_CODE);
            verify(feeService).getFeesDataForC2(WITHOUT_NOTICE);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldMakeExpectedPaymentWhenFeeAndPayCaseTypeFeatureToggleIsToggledOnAndOff(final boolean toggleStatus) {
            CaseData caseData = CaseData.builder()
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .c2DocumentBundle(List.of(element(C2DocumentBundle.builder()
                    .type(WITH_NOTICE)
                    .pbaNumber(PBA_NUMBER)
                    .clientCode(CLIENT_CODE)
                    .fileReference(CUSTOMER_REFERENCE)
                    .build())))
                .build();

            when(featureToggleService.isFeeAndPayCaseTypeEnabled()).thenReturn(toggleStatus);

            CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
                .customerReference(CUSTOMER_REFERENCE)
                .amount(feeForC2WithNotice.getCalculatedAmount())
                .fees(List.of(feeForC2WithNotice))
                .build();

            paymentService.makePaymentForC2(CASE_ID, caseData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName(LOCAL_AUTHORITY_CODE);
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
        FeeDto feeForAdditionalApplications = FeeDto.builder().calculatedAmount(BigDecimal.TEN).build();
        FeesData feesData = buildFeesData(feeForAdditionalApplications);

        @Test
        void shouldMakeCorrectPaymentForAdditionalApplications() {
            CaseData caseData = buildCaseData(CLIENT_CODE, CUSTOMER_REFERENCE);

            CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
                .customerReference("customerReference")
                .organisationName("Swansea City Council")
                .amount(feeForAdditionalApplications.getCalculatedAmount())
                .fees(List.of(feeForAdditionalApplications))
                .build();

            paymentService.makePaymentForAdditionalApplications(CASE_ID, caseData, feesData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
        }

        @ParameterizedTest
        @MethodSource("getC2BundleSubfields")
        void shouldMakeCorrectPaymentForConfidentialAdditionalApplications(String confFieldName) {
            CaseData caseData = buildConfidentialCaseData(CLIENT_CODE, CUSTOMER_REFERENCE,
                "Swansea City Council, Applicant", confFieldName);

            CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
                .customerReference("customerReference")
                .organisationName("Swansea City Council")
                .amount(feeForAdditionalApplications.getCalculatedAmount())
                .fees(List.of(feeForAdditionalApplications))
                .build();

            paymentService.makePaymentForAdditionalApplications(CASE_ID, caseData, feesData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
        }

        @Test
        void shouldMakeCorrectPaymentForAdditionalApplicationsWithCorrectNameIfRespondent() {
            CaseData caseData = buildCaseData(CLIENT_CODE, CUSTOMER_REFERENCE, "John Smith, Respondent 1");

            CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
                .customerReference("customerReference")
                .organisationName("On behalf of John Smith, Respondent 1")
                .amount(feeForAdditionalApplications.getCalculatedAmount())
                .fees(List.of(feeForAdditionalApplications))
                .build();

            paymentService.makePaymentForAdditionalApplications(CASE_ID, caseData, feesData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
        }

        @Test
        void shouldMakeCorrectPaymentForAdditionalApplicationsWithCorrectNameIfSecondaryLA() {
            final String secondaryLA = "Devon County Council";
            CaseData caseData = buildCaseData(CLIENT_CODE, CUSTOMER_REFERENCE, secondaryLA + ", Secondary LA");

            CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
                .customerReference("customerReference")
                .organisationName(secondaryLA)
                .amount(feeForAdditionalApplications.getCalculatedAmount())
                .fees(List.of(feeForAdditionalApplications))
                .build();

            paymentService.makePaymentForAdditionalApplications(CASE_ID, caseData, feesData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldMakeCorrectPaymentForAdditionalApplicationsWhenCustomerReferenceIsInvalid(
            final String customerReference) {

            CaseData caseData = buildCaseData(CLIENT_CODE, customerReference);

            CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
                .organisationName("Swansea City Council")
                .customerReference(BLANK_PARAMETER_VALUE)
                .amount(feeForAdditionalApplications.getCalculatedAmount())
                .fees(List.of(FeeDto.builder().calculatedAmount(BigDecimal.TEN).build()))
                .build();

            paymentService.makePaymentForAdditionalApplications(CASE_ID, caseData, feesData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldMakeCorrectPaymentForAdditionalApplicationsWhenCaseReferenceIsInvalid(final String clientCode) {
            CaseData caseData = buildCaseData(clientCode, CUSTOMER_REFERENCE);

            CreditAccountPaymentRequest expectedPaymentRequest = buildCreditAccountPaymentRequest(clientCode,
                CUSTOMER_REFERENCE,
                feeForAdditionalApplications);

            expectedPaymentRequest.setOrganisationName("Swansea City Council");

            paymentService.makePaymentForAdditionalApplications(CASE_ID, caseData, feesData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldMakeExpectedPaymentWhenFeeAndPayCaseTypeFeatureToggleIsToggledOnAndOff(final boolean toggleStatus) {
            CaseData caseData = buildCaseData(CLIENT_CODE, CUSTOMER_REFERENCE);

            when(featureToggleService.isFeeAndPayCaseTypeEnabled()).thenReturn(toggleStatus);

            CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
                .organisationName("Swansea City Council")
                .customerReference(CUSTOMER_REFERENCE)
                .amount(feeForAdditionalApplications.getCalculatedAmount())
                .fees(List.of(feeForAdditionalApplications))
                .build();

            paymentService.makePaymentForAdditionalApplications(CASE_ID, caseData, feesData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
        }

        private CaseData buildCaseData(String clientCode, String customerReference) {
            return buildCaseData(clientCode, customerReference, "Swansea City Council, Applicant");
        }

        private CaseData buildCaseData(String clientCode, String customerReference, String applicantName) {
            return CaseData.builder()
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .additionalApplicationsBundle(List.of(
                    element(AdditionalApplicationsBundle.builder()
                        .c2DocumentBundle(C2DocumentBundle.builder()
                            .applicantName(applicantName)
                            .build())
                        .pbaPayment(PBAPayment.builder()
                            .clientCode(clientCode)
                            .fileReference(customerReference)
                            .pbaNumber(PBA_NUMBER)
                            .build()).build()))).build();
        }

        private CaseData buildConfidentialCaseData(String clientCode, String customerReference, String applicantName,
                                                   String fieldName) {
            AdditionalApplicationsBundle appsBundle = AdditionalApplicationsBundle.builder()
                .pbaPayment(PBAPayment.builder()
                    .clientCode(clientCode)
                    .fileReference(customerReference)
                    .pbaNumber(PBA_NUMBER)
                    .build()).build();

            ReflectionTestUtils.setField(appsBundle, fieldName, C2DocumentBundle.builder()
                .applicantName(applicantName)
                .build());

            return CaseData.builder()
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .additionalApplicationsBundle(List.of(
                    element(appsBundle))).build();
        }

        private FeesData buildFeesData(FeeDto feeDto) {
            return FeesData.builder()
                .totalAmount(feeDto.getCalculatedAmount())
                .fees(List.of(feeDto))
                .build();
        }

        private static Stream<Arguments> getC2BundleSubfields() {
            Stream.Builder<Arguments> stream = Stream.builder();

            stream.add(Arguments.of("c2DocumentBundle"));
            stream.add(Arguments.of("c2DocumentBundleLA"));
            stream.add(Arguments.of("c2DocumentBundleConfidential"));
            for (int i = 0; i < 9; i++) {
                stream.add(Arguments.of("c2DocumentBundleResp" + i));
            }
            for (int i = 0; i < 15; i++) {
                stream.add(Arguments.of("c2DocumentBundleChild" + i));
            }
            return stream.build();
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
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .localAuthorities(wrapElements(localAuthority))
                .applicants(List.of(element(Applicant.builder()
                    .party(ApplicantParty.builder()
                        .pbaNumber(PBA_NUMBER)
                        .clientCode(CLIENT_CODE)
                        .customerReference(CUSTOMER_REFERENCE)
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
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName(LOCAL_AUTHORITY_CODE);
            verify(feeService).getFeesDataForOrders(orders);
        }

        @Test
        void shouldTakePaymentContextDetailsFromLocalAuthorityDetailsWhenCaseLocalAuthorityIsNull() {
            when(feeService.getFeesDataForOrders(orders)).thenReturn(FeesData.builder()
                .totalAmount(BigDecimal.TEN)
                .fees(List.of(careOrderFee, supervisionOrderFee))
                .build());

            final LocalAuthority localAuthority = LocalAuthority.builder()
                .name("Example Local Authority")
                .designated("No")
                .pbaNumber("PBA1234567")
                .customerReference("localAuthorityReference")
                .clientCode("localAuthorityCode")
                .build();

            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .caseLocalAuthority(null)
                .localAuthorities(wrapElements(localAuthority))
                .applicants(List.of(element(Applicant.builder()
                    .party(ApplicantParty.builder()
                        .pbaNumber(PBA_NUMBER)
                        .clientCode(CLIENT_CODE)
                        .customerReference(CUSTOMER_REFERENCE)
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
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .applicants(List.of(element(Applicant.builder()
                    .party(ApplicantParty.builder()
                        .pbaNumber(PBA_NUMBER)
                        .clientCode(CLIENT_CODE)
                        .customerReference(CUSTOMER_REFERENCE)
                        .build())
                    .build())))
                .orders(orders)
                .build();

            CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
                .customerReference(CUSTOMER_REFERENCE)
                .amount(BigDecimal.TEN)
                .fees(List.of(careOrderFee, supervisionOrderFee))
                .build();

            paymentService.makePaymentForCaseOrders(caseData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName(LOCAL_AUTHORITY_CODE);
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

            when(feeService.getFeesDataForOrders(orders)).thenReturn(FeesData.builder()
                .totalAmount(BigDecimal.TEN)
                .fees(List.of(careOrderFee, supervisionOrderFee))
                .build());

            CaseData caseData = buildCaseDataForC110Application(CLIENT_CODE, customerReference, orders);

            CreditAccountPaymentRequest expectedPaymentRequest = buildCreditAccountPaymentRequestForC110Application(
                CLIENT_CODE,
                customerReference);

            paymentService.makePaymentForCaseOrders(caseData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName(LOCAL_AUTHORITY_CODE);
            verify(feeService).getFeesDataForOrders(orders);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldMakeCorrectPaymentForC110AWhenCaseReferenceIsInvalid(final String clientCode) {

            when(feeService.getFeesDataForOrders(orders)).thenReturn(FeesData.builder()
                .totalAmount(BigDecimal.TEN)
                .fees(List.of(careOrderFee, supervisionOrderFee))
                .build());

            CaseData caseData = buildCaseDataForC110Application(clientCode, CUSTOMER_REFERENCE, orders);

            CreditAccountPaymentRequest expectedPaymentRequest = buildCreditAccountPaymentRequestForC110Application(
                clientCode,
                CUSTOMER_REFERENCE);

            paymentService.makePaymentForCaseOrders(caseData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName(LOCAL_AUTHORITY_CODE);
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
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .applicants(List.of(element(Applicant.builder()
                    .party(ApplicantParty.builder()
                        .pbaNumber(PBA_NUMBER)
                        .clientCode(CLIENT_CODE)
                        .customerReference(CUSTOMER_REFERENCE)
                        .build())
                    .build())))
                .orders(orders)
                .build();

            when(featureToggleService.isFeeAndPayCaseTypeEnabled()).thenReturn(toggleStatus);

            CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
                .customerReference(CUSTOMER_REFERENCE)
                .amount(BigDecimal.TEN)
                .fees(List.of(careOrderFee, supervisionOrderFee))
                .build();

            paymentService.makePaymentForCaseOrders(caseData);

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
            verify(localAuthorityNameLookupConfiguration).getLocalAuthorityName(LOCAL_AUTHORITY_CODE);
            verify(feeService).getFeesDataForOrders(orders);
        }
    }

    @Nested
    class MakePaymentForPlacement {

        private final String applicant = "Applicant 1";

        private final FeeDto placementFee = FeeDto.builder()
            .calculatedAmount(BigDecimal.TEN)
            .build();

        @BeforeEach
        void init() {

            when(feeService.getFeesDataForPlacement()).thenReturn(FeesData.builder()
                .totalAmount(BigDecimal.TEN)
                .fees(List.of(placementFee))
                .build());
        }

        @Test
        void shouldMakePaymentForPlacement() {

            final PBAPayment pbaPayment = PBAPayment.builder()
                .pbaNumber(PBA_NUMBER)
                .clientCode(CLIENT_CODE)
                .fileReference(CUSTOMER_REFERENCE)
                .build();

            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .placementEventData(PlacementEventData.builder().placementPayment(pbaPayment).build())
                .build();

            paymentService.makePaymentForPlacement(caseData, applicant);

            final CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
                .accountNumber(PBA_NUMBER)
                .customerReference(CUSTOMER_REFERENCE)
                .caseReference(CLIENT_CODE)
                .organisationName(applicant)
                .amount(BigDecimal.TEN)
                .fees(List.of(placementFee))
                .build();

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
        }

        @Test
        void shouldMakePaymentForPlacementWithoutClientCodeAndCustomerReference() {

            final PBAPayment pbaPayment = PBAPayment.builder()
                .pbaNumber(PBA_NUMBER)
                .build();

            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .placementEventData(PlacementEventData.builder().placementPayment(pbaPayment).build())
                .build();

            paymentService.makePaymentForPlacement(caseData, applicant);

            final CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
                .accountNumber(PBA_NUMBER)
                .customerReference(NOT_PROVIDED)
                .caseReference(NOT_PROVIDED)
                .amount(BigDecimal.TEN)
                .fees(List.of(placementFee))
                .organisationName(applicant)
                .build();

            verify(paymentClient).callPaymentsApi(expectedPaymentRequest);
        }

        @Test
        void shouldThrowsExceptionWhenPaymentDetailsNotPresent() {

            final CaseData caseData = CaseData.builder()
                .id(CASE_ID)
                .placementEventData(PlacementEventData.builder().build())
                .build();

            assertThatThrownBy(() -> paymentService.makePaymentForPlacement(caseData, applicant))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Case does not have PBA number for placement payment");
        }

        @Test
        void shouldThrowsExceptionWhenPBANumberNotPresent() {

            final PBAPayment pbaPayment = PBAPayment.builder()
                .build();

            final CaseData caseData = CaseData.builder()
                .placementEventData(PlacementEventData.builder()
                    .placementPayment(pbaPayment)
                    .build())
                .build();

            assertThatThrownBy(() -> paymentService.makePaymentForPlacement(caseData, applicant))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Case does not have PBA number for placement payment");
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
            .accountNumber(PBA_NUMBER)
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
        return creditAccountPaymentRequestBuilder().caseReference(CLIENT_CODE);
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
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .c2DocumentBundle(List.of(element(C2DocumentBundle.builder()
                .type(type)
                .pbaNumber(PBA_NUMBER)
                .clientCode(clientCode)
                .fileReference(customerReference)
                .build())))
            .build();
    }

    private CaseData buildCaseDataForC110Application(String clientCode, String customerReference, Orders orders) {
        return CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .localAuthorities(wrapElements(LocalAuthority.builder()
                .designated("Yes")
                .pbaNumber(PBA_NUMBER)
                .clientCode(clientCode)
                .customerReference(customerReference)
                .build()))
            .orders(orders)
            .build();
    }


}
