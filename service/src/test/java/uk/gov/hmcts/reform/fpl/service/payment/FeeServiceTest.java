package uk.gov.hmcts.reform.fpl.service.payment;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fnp.client.FeesRegisterApi;
import uk.gov.hmcts.reform.fnp.model.fee.FeeResponse;
import uk.gov.hmcts.reform.fnp.model.fee.FeeType;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.CARE_ORDER;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    FeeService.class, LookupTestConfig.class,
})
class FeeServiceTest {

    @MockBean
    private FeesRegisterApi feesRegisterApi;

    @Autowired
    private FeeService feeService;

    @Nested
    class MakeRequest {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnAnEmptyListWhenNullOrEmptyListIsPassed(List<FeeType> list) {
            assertThat(feeService.getFees(list)).isEmpty();
        }

        @ParameterizedTest
        @EnumSource(FeeType.class)
        void shouldGetAFeeResponseWhenGivenAPopulated(FeeType feeType) {
            // TODO: 17/02/2020 mock something

            List<FeeResponse> fees = feeService.getFees(List.of(feeType));
            FeeResponse actual = fees.get(0);

            assertThat(fees).hasSize(1);
            assertThat(actual.getCode()).isEqualTo("FEE0327");
            assertThat(actual.getDescription()).isEqualTo("example");
            assertThat(actual.getVersion()).isEqualTo(1);
            assertThat(actual.getAmount()).isEqualTo(BigDecimal.valueOf(255));
            FeeResponse feeResponse = new FeeResponse();
        }

        @Test
        void shouldFilterOutNullValuesWhenThereIsAnErrorInTheResponse() {
            // TODO: 17/02/2020 mock something

            List<FeeResponse> feeResponses = feeService.getFees(List.of(CARE_ORDER));

            assertThat(feeResponses).isEmpty();
        }

        private String buildURI() {
            return "http://localhost:8080/fees-register/fees/lookup?service=private%20law&jurisdiction1=family"
                + "&jurisdiction2=family%20court&channel=default&event=miscellaneous&keyword=KLM";
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
            Optional<FeeResponse> actual = feeService.extractFeeToUse(feeResponses);
            assertThat(actual).isPresent();
            assertThat(actual.get()).isEqualTo(feeResponses.get(1));
        }

        private FeeResponse buildFee(double amount) {
            FeeResponse response = new FeeResponse();
            response.setAmount(BigDecimal.valueOf(amount));
            return response;
        }
    }
}
