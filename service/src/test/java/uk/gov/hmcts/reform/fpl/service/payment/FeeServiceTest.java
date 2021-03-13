package uk.gov.hmcts.reform.fpl.service.payment;

import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fnp.client.FeesRegisterApi;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fnp.model.fee.FeeResponse;
import uk.gov.hmcts.reform.fnp.model.fee.FeeType;
import uk.gov.hmcts.reform.fnp.model.payment.FeeDto;
import uk.gov.hmcts.reform.fpl.enums.C2ApplicationType;
import uk.gov.hmcts.reform.fpl.enums.C2OrdersRequested;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.enums.ParentalResponsibilityType;
import uk.gov.hmcts.reform.fpl.enums.SecureAccommodationType;
import uk.gov.hmcts.reform.fpl.enums.Supplements;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.testbeans.TestFeeConfig;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.CARE_ORDER;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.OTHER;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.PLACEMENT;
import static uk.gov.hmcts.reform.fpl.enums.C2OrdersRequested.PARENTAL_RESPONSIBILITY;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_CHANGE_SURNAME_OR_REMOVE_FROM_JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.ParentalResponsibilityType.PR_BY_FATHER;
import static uk.gov.hmcts.reform.fpl.enums.Supplements.C13A_SPECIAL_GUARDIANSHIP;
import static uk.gov.hmcts.reform.fpl.enums.Supplements.C16_CHILD_ASSESSMENT;
import static uk.gov.hmcts.reform.fpl.enums.Supplements.C18_RECOVERY_ORDER;
import static uk.gov.hmcts.reform.fpl.testbeans.TestFeeConfig.C2_WITHOUT_NOTICE_KEYWORD;
import static uk.gov.hmcts.reform.fpl.testbeans.TestFeeConfig.C2_WITH_NOTICE_KEYWORD;
import static uk.gov.hmcts.reform.fpl.testbeans.TestFeeConfig.CARE_ORDER_KEYWORD;
import static uk.gov.hmcts.reform.fpl.testbeans.TestFeeConfig.CHANGE_SURNAME_KEYWORD;
import static uk.gov.hmcts.reform.fpl.testbeans.TestFeeConfig.CHANNEL;
import static uk.gov.hmcts.reform.fpl.testbeans.TestFeeConfig.CHILD_ASSESSMENT_KEYWORD;
import static uk.gov.hmcts.reform.fpl.testbeans.TestFeeConfig.EVENT;
import static uk.gov.hmcts.reform.fpl.testbeans.TestFeeConfig.JURISDICTION_1;
import static uk.gov.hmcts.reform.fpl.testbeans.TestFeeConfig.JURISDICTION_2;
import static uk.gov.hmcts.reform.fpl.testbeans.TestFeeConfig.OTHER_KEYWORD;
import static uk.gov.hmcts.reform.fpl.testbeans.TestFeeConfig.PLACEMENT_KEYWORD;
import static uk.gov.hmcts.reform.fpl.testbeans.TestFeeConfig.PR_FATHER_KEYWORD;
import static uk.gov.hmcts.reform.fpl.testbeans.TestFeeConfig.PR_FEMALE_PARENT_KEYWORD;
import static uk.gov.hmcts.reform.fpl.testbeans.TestFeeConfig.RECOVERY_ORDER_KEYWORD;
import static uk.gov.hmcts.reform.fpl.testbeans.TestFeeConfig.SECURE_ACCOMMODATION_ENG_KEYWORD;
import static uk.gov.hmcts.reform.fpl.testbeans.TestFeeConfig.SECURE_ACCOMMODATION_WALES_KEYWORD;
import static uk.gov.hmcts.reform.fpl.testbeans.TestFeeConfig.SERVICE;
import static uk.gov.hmcts.reform.fpl.testbeans.TestFeeConfig.SUPERVISION_ORDER_KEYWORD;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    FeeService.class, TestFeeConfig.class,
})
class FeeServiceTest {

    @MockBean
    private FeesRegisterApi feesRegisterApi;

    @Autowired
    private FeeService feeService;

    @Nested
    class GetFees {
        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnAnEmptyListWhenNullOrEmptyListIsPassed(List<FeeType> list) {
            assertThat(feeService.getFees(list)).isEmpty();
        }

        @Test
        void shouldGetAFeeResponseWhenGivenAPopulated() {
            FeeResponse careOrderResponse = expectedFeeResponse(123);
            FeeResponse otherResponse = expectedFeeResponse(231);
            FeeResponse placementResponse = expectedFeeResponse(321);

            when(feesRegisterApi.findFee(CHANNEL, EVENT, JURISDICTION_1, JURISDICTION_2, CARE_ORDER_KEYWORD, SERVICE))
                .thenReturn(careOrderResponse);
            when(feesRegisterApi.findFee(CHANNEL, EVENT, JURISDICTION_1, JURISDICTION_2, OTHER_KEYWORD, SERVICE))
                .thenReturn(otherResponse);
            when(feesRegisterApi.findFee(CHANNEL, EVENT, JURISDICTION_1, JURISDICTION_2, PLACEMENT_KEYWORD, SERVICE))
                .thenReturn(placementResponse);

            List<FeeResponse> fees = feeService.getFees(List.of(CARE_ORDER, OTHER, PLACEMENT));

            assertThat(fees).containsOnly(careOrderResponse, otherResponse, placementResponse);
        }

        @Test
        void shouldPropagateExceptionWhenThereIsAnErrorInTheResponse() {
            when(feesRegisterApi.findFee(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new FeignException.BadRequest(
                    "", Request.create(GET, EMPTY, Map.of(), new byte[]{}, UTF_8, null), new byte[]{})
                );

            List<FeeType> feeTypes = List.of(CARE_ORDER);
            assertThrows(FeeRegisterException.class, () -> feeService.getFees(feeTypes));
        }

        @AfterEach
        void resetInvocations() {
            reset(feesRegisterApi);
        }

        private FeeResponse expectedFeeResponse(double amount) {
            FeeResponse feeResponse = new FeeResponse();
            feeResponse.setCode("FEE0327");
            feeResponse.setAmount(BigDecimal.valueOf(amount));
            feeResponse.setDescription("example");
            feeResponse.setVersion(1);
            return feeResponse;
        }
    }

    @Nested
    class ExtractFeeToUse {
        @ParameterizedTest
        @NullAndEmptySource
        void shouldThrowNoSuchElementExceptionWhenPassedAnNullOrEmptyList(List<FeeResponse> list) {
            assertThat(feeService.extractFeeToUse(list)).isEmpty();
        }

        @Test
        void shouldReturnTheFeeResponseWithMaxFeeWhenPassedAPopulatedList() {
            List<FeeResponse> feeResponses = List.of(buildFee(12), buildFee(73.4), buildFee(45));
            Optional<FeeResponse> mostExpensive = feeService.extractFeeToUse(feeResponses);
            assertThat(mostExpensive).isPresent().contains(feeResponses.get(1));
        }

        private FeeResponse buildFee(double amount) {
            FeeResponse response = new FeeResponse();
            response.setAmount(BigDecimal.valueOf(amount));
            return response;
        }
    }

    @Nested
    class GetFeesDataForOrders {
        private static final String CARE_ORDER_CODE = "FEE001";
        private static final String SUPERVISION_ORDER_CODE = "FEE002";

        @BeforeEach
        void setup() {
            when(feesRegisterApi.findFee(CHANNEL,
                EVENT,
                JURISDICTION_1,
                JURISDICTION_2,
                CARE_ORDER_KEYWORD,
                SERVICE)).thenReturn(buildFeeResponse(CARE_ORDER_CODE, BigDecimal.ONE));
            when(feesRegisterApi.findFee(CHANNEL,
                EVENT,
                JURISDICTION_1,
                JURISDICTION_2,
                SUPERVISION_ORDER_KEYWORD,
                SERVICE)).thenReturn(buildFeeResponse(SUPERVISION_ORDER_CODE, BigDecimal.TEN));
        }

        @Test
        void shouldPropagateExceptionWhenThereIsAnErrorInTheResponse() {
            when(feesRegisterApi.findFee(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new FeignException.BadRequest(
                    "", Request.create(GET, EMPTY, Map.of(), new byte[]{}, UTF_8, null), new byte[]{})
                );

            Orders orders = Orders.builder()
                .orderType(List.of(OrderType.CARE_ORDER)).build();
            assertThrows(FeeRegisterException.class, () -> feeService.getFeesDataForOrders(orders));
        }

        @Test
        void shouldReturnCorrectFeesDataForOrders() {
            Orders orders = Orders.builder()
                .orderType(List.of(OrderType.CARE_ORDER, OrderType.SUPERVISION_ORDER))
                .build();

            FeesData feesData = feeService.getFeesDataForOrders(orders);
            List<FeeDto> fees = feesData.getFees();

            assertThat(feesData.getTotalAmount()).isEqualTo(BigDecimal.TEN);
            assertThat(fees).hasSize(1);
            assertThat(fees.get(0).getCode()).isEqualTo(SUPERVISION_ORDER_CODE);
            assertThat(fees.get(0).getCalculatedAmount()).isEqualTo(BigDecimal.TEN);
        }

        @AfterEach
        void resetInvocations() {
            reset(feesRegisterApi);
        }
    }

    @Nested
    class GetFeesDataForC2 {

        private static final String WITH_NOTICE_FEE_CODE = "FEE500";
        private static final String WITHOUT_NOTICE_FEE_CODE = "FEE100";

        @BeforeEach
        void setup() {
            when(feesRegisterApi.findFee(CHANNEL,
                EVENT,
                JURISDICTION_1,
                JURISDICTION_2,
                C2_WITH_NOTICE_KEYWORD,
                SERVICE)).thenReturn(buildFeeResponse(WITH_NOTICE_FEE_CODE, BigDecimal.ONE));
            when(feesRegisterApi.findFee(CHANNEL,
                EVENT,
                JURISDICTION_1,
                JURISDICTION_2,
                C2_WITHOUT_NOTICE_KEYWORD,
                SERVICE)).thenReturn(buildFeeResponse(WITHOUT_NOTICE_FEE_CODE, BigDecimal.TEN));
        }

        @Test
        void shouldReturnCorrespondingFeesDataForC2ApplicationType() {
            FeesData feesData = feeService.getFeesDataForC2(C2ApplicationType.WITH_NOTICE);
            assertThat(feesData.getTotalAmount()).isEqualTo(BigDecimal.ONE);
            assertThat(getFirstFeeCode(feesData)).isEqualTo(WITH_NOTICE_FEE_CODE);

            feesData = feeService.getFeesDataForC2(C2ApplicationType.WITHOUT_NOTICE);
            assertThat(feesData.getTotalAmount()).isEqualTo(BigDecimal.TEN);
            assertThat(getFirstFeeCode(feeService.getFeesDataForC2(C2ApplicationType.WITHOUT_NOTICE))).isEqualTo(
                WITHOUT_NOTICE_FEE_CODE);
        }

        private String getFirstFeeCode(FeesData feesData) {
            return feesData.getFees().get(0).getCode();
        }

        @AfterEach
        void resetInvocations() {
            reset(feesRegisterApi);
        }
    }

    @Nested
    class GetFeesDataForOtherApplications {

        private static final String WITH_NOTICE_FEE_CODE = "FEE0300";
        private static final String CHANGE_SURNAME = "FEE0330";
        private static final String CHILD_ASSESSMENT = "FEE0326";
        private static final String RECOVERY_ORDER = "FEE0323";
        private static final String SECURE_ACCOMMODATION_WALES = "FEE0313";
        private static final String SECURE_ACCOMMODATION_ENGLAND = "FEE0314";
        private static final String PARENTAL_RESPONSIBILITY_FATHER = "FEE0320";
        private static final String PARENTAL_RESPONSIBILITY_FEMALE_PARENT = "FEE0322";

        @BeforeEach
        void setup() {
            when(feesRegisterApi.findFee(CHANNEL, EVENT, JURISDICTION_1, JURISDICTION_2, C2_WITH_NOTICE_KEYWORD,
                SERVICE)).thenReturn(buildFeeResponse(WITH_NOTICE_FEE_CODE, BigDecimal.valueOf(20)));

            when(feesRegisterApi.findFee(CHANNEL, EVENT, JURISDICTION_1, JURISDICTION_2, CHANGE_SURNAME_KEYWORD,
                SERVICE)).thenReturn(buildFeeResponse(CHANGE_SURNAME, BigDecimal.valueOf(50)));

            when(feesRegisterApi.findFee(CHANNEL, EVENT, JURISDICTION_1, JURISDICTION_2, CHILD_ASSESSMENT_KEYWORD,
                SERVICE)).thenReturn(buildFeeResponse(CHILD_ASSESSMENT, BigDecimal.valueOf(60)));

            when(feesRegisterApi.findFee(CHANNEL, EVENT, JURISDICTION_1, JURISDICTION_2, RECOVERY_ORDER_KEYWORD,
                SERVICE)).thenReturn(buildFeeResponse(RECOVERY_ORDER, BigDecimal.valueOf(25)));

            when(feesRegisterApi.findFee(CHANNEL, EVENT, JURISDICTION_1, JURISDICTION_2,
                SECURE_ACCOMMODATION_ENG_KEYWORD, SERVICE))
                .thenReturn(buildFeeResponse(SECURE_ACCOMMODATION_ENGLAND, BigDecimal.valueOf(30)));

            when(feesRegisterApi.findFee(CHANNEL, EVENT, JURISDICTION_1, JURISDICTION_2,
                SECURE_ACCOMMODATION_WALES_KEYWORD, SERVICE))
                .thenReturn(buildFeeResponse(SECURE_ACCOMMODATION_WALES, BigDecimal.valueOf(75)));

            when(feesRegisterApi.findFee(CHANNEL, EVENT, JURISDICTION_1, JURISDICTION_2, PR_FEMALE_PARENT_KEYWORD,
                SERVICE)).thenReturn(buildFeeResponse(PARENTAL_RESPONSIBILITY_FEMALE_PARENT, BigDecimal.valueOf(30)));

            when(feesRegisterApi.findFee(CHANNEL, EVENT, JURISDICTION_1, JURISDICTION_2, PR_FATHER_KEYWORD, SERVICE))
                .thenReturn(buildFeeResponse(PARENTAL_RESPONSIBILITY_FATHER, BigDecimal.valueOf(35)));
        }

        @Test
        void shouldReturnFeesDataWithMaximumAmountForOtherApplicationTypeAndSupplements() {
            FeesData feesData = feeService.getFeesDataForAdditionalApplications(
                null,
                buildOtherApplicationsBundle(C1_CHANGE_SURNAME_OR_REMOVE_FROM_JURISDICTION, null),
                List.of(C16_CHILD_ASSESSMENT, C18_RECOVERY_ORDER),
                List.of());

            assertThat(feesData.getTotalAmount()).isEqualTo(BigDecimal.valueOf(60));
            assertThat(getFirstFeeCode(feesData)).isEqualTo(CHILD_ASSESSMENT);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnApplicationFeesDataWhenNoSupplementsExist(List<Supplements> supplementTypes) {
            FeesData feesData = feeService.getFeesDataForAdditionalApplications(
                null,
                buildOtherApplicationsBundle(C1_CHANGE_SURNAME_OR_REMOVE_FROM_JURISDICTION, null),
                supplementTypes, List.of());

            assertThat(feesData.getTotalAmount()).isEqualTo(BigDecimal.valueOf(50));
            assertThat(getFirstFeeCode(feesData)).isEqualTo(CHANGE_SURNAME);
        }

        @Test
        void shouldReturnFeesDataWithMaximumAmountForSupplementsWithSecureAccommodationWales() {
            FeesData feesData = feeService.getFeesDataForAdditionalApplications(
                null,
                buildOtherApplicationsBundle(C1_CHANGE_SURNAME_OR_REMOVE_FROM_JURISDICTION, null),
                List.of(C16_CHILD_ASSESSMENT, C18_RECOVERY_ORDER),
                List.of(SecureAccommodationType.SECTION_119_WALES));

            assertThat(feesData.getTotalAmount()).isEqualTo(BigDecimal.valueOf(75));
            assertThat(getFirstFeeCode(feesData)).isEqualTo(SECURE_ACCOMMODATION_WALES);
        }

        @Test
        void shouldReturnFeesDataWithMaximumAmountForSupplementsWithSecureAccommodationEngland() {
            FeesData feesData = feeService.getFeesDataForAdditionalApplications(
                null,
                buildOtherApplicationsBundle(C1_CHANGE_SURNAME_OR_REMOVE_FROM_JURISDICTION, null),
                List.of(C16_CHILD_ASSESSMENT, C18_RECOVERY_ORDER),
                List.of(SecureAccommodationType.SECTION_25_ENGLAND));

            assertThat(feesData.getTotalAmount()).isEqualTo(BigDecimal.valueOf(60));
            assertThat(getFirstFeeCode(feesData)).isEqualTo(CHILD_ASSESSMENT);
        }

        @Test
        void shouldReturnFeesDataWithMaximumAmountForParentalResponsibilityType() {
            FeesData feesData = feeService.getFeesDataForAdditionalApplications(
                buildC2Document(C2ApplicationType.WITH_NOTICE, List.of(C2OrdersRequested.APPOINTMENT_OF_GUARDIAN)),
                buildOtherApplicationsBundle(
                    OtherApplicationType.C1_PARENTAL_RESPONSIBILITY, PR_BY_FATHER),
                List.of(C13A_SPECIAL_GUARDIANSHIP),
                List.of());

            assertThat(feesData.getTotalAmount()).isEqualTo(BigDecimal.valueOf(35));
            assertThat(getFirstFeeCode(feesData)).isEqualTo(PARENTAL_RESPONSIBILITY_FATHER);
        }

        @Test
        void shouldReturnFeesDataWithMaximumAmountWhenC2OrdersRequestedHaveParentalResponsibilityType() {
            FeesData feesData = feeService.getFeesDataForAdditionalApplications(
                buildC2Document(C2ApplicationType.WITH_NOTICE, List.of(PARENTAL_RESPONSIBILITY), PR_BY_FATHER),
                null,
                List.of(C13A_SPECIAL_GUARDIANSHIP),
                List.of());

            assertThat(feesData.getTotalAmount()).isEqualTo(BigDecimal.valueOf(35));
            assertThat(getFirstFeeCode(feesData)).isEqualTo(PARENTAL_RESPONSIBILITY_FATHER);
        }

        private String getFirstFeeCode(FeesData feesData) {
            return feesData.getFees().get(0).getCode();
        }

        @AfterEach
        void resetInvocations() {
            reset(feesRegisterApi);
        }
    }

    private FeeResponse buildFeeResponse(String code, BigDecimal amount) {
        FeeResponse feeResponse = new FeeResponse();
        feeResponse.setCode(code);
        feeResponse.setAmount(amount);
        feeResponse.setVersion(1);
        feeResponse.setDescription("test description");
        return feeResponse;
    }

    private C2DocumentBundle buildC2Document(C2ApplicationType type, List<C2OrdersRequested> c2Orders) {
        return buildC2Document(type, c2Orders, null);
    }

    private C2DocumentBundle buildC2Document(
        C2ApplicationType type, List<C2OrdersRequested> c2Orders, ParentalResponsibilityType prType) {
        return C2DocumentBundle.builder().type(type).c2OrdersRequested(c2Orders)
            .parentalResponsibilityType(prType)
            .build();
    }

    private OtherApplicationsBundle buildOtherApplicationsBundle(
        OtherApplicationType type, ParentalResponsibilityType prType) {
        return OtherApplicationsBundle.builder().applicationType(type).parentalResponsibilityType(prType).build();
    }
}
