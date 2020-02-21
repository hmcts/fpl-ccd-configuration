package uk.gov.hmcts.reform.fpl.service.payment;

import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.AfterEach;
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
import uk.gov.hmcts.reform.fnp.model.fee.FeeResponse;
import uk.gov.hmcts.reform.fnp.model.fee.FeeType;
import uk.gov.hmcts.reform.fpl.testbeans.TestFeeConfig;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.CARE_ORDER;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.PLACEMENT;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.SUPERVISION_ORDER;

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
    class MakeRequest {
        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnAnEmptyListWhenNullOrEmptyListIsPassed(List<FeeType> list) {
            assertThat(feeService.getFees(list)).isEmpty();
        }

        @Test
        void shouldGetAFeeResponseWhenGivenAPopulated() {
            FeeResponse expectedFeeResponse = expectedFeeResponse();

            when(feesRegisterApi.findFee("default", "miscellaneous", "family", "family court", "KLM", "private law"))
                .thenReturn(expectedFeeResponse);

            List<FeeResponse> fees = feeService.getFees(List.of(CARE_ORDER, SUPERVISION_ORDER, PLACEMENT));

            assertThat(fees).hasSize(3);
            assertThat(fees).containsOnly(expectedFeeResponse);
        }

        @Test
        void shouldPropagateExceptionWhenThereIsAnErrorInTheResponse() {
            when(feesRegisterApi.findFee(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new FeignException.BadRequest(
                    "", Request.create(GET, EMPTY, Map.of(), new byte[]{}, UTF_8), new byte[]{})
                );

            assertThrows(FeignException.class, () -> feeService.getFees(List.of(CARE_ORDER)));
        }

        @AfterEach
        void resetInvocations() {
            reset(feesRegisterApi);
        }

        private FeeResponse expectedFeeResponse() {
            FeeResponse feeResponse = new FeeResponse();
            feeResponse.setCode("FEE0327");
            feeResponse.setAmount(BigDecimal.valueOf(255));
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
