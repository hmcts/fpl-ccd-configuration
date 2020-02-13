package uk.gov.hmcts.reform.fpl.service.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.fpl.enums.payment.FeeType;
import uk.gov.hmcts.reform.fpl.model.payment.fee.FeeResponse;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static uk.gov.hmcts.reform.fpl.enums.payment.FeeType.CARE_ORDER;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    FeeService.class, RestTemplate.class, LookupTestConfig.class
})
class FeeServiceTest {

    @Autowired
    private FeeService feeService;

    @Autowired
    private RestTemplate restTemplate;

    @Nested
    class MakeRequest {
        private MockRestServiceServer mockServer;

        @BeforeEach
        void setUp() {
            mockServer = MockRestServiceServer.createServer(restTemplate);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnAnEmptyListWhenNullOrEmptyListIsPassed(List<FeeType> list) {
            assertThat(feeService.getFees(list)).isEmpty();
        }

        @ParameterizedTest
        @EnumSource(FeeType.class)
        void shouldGetAFeeResponseWhenGivenAPopulated(FeeType feeType) {
            mockServer.expect(requestTo(buildURI()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{ \"code\" : \"FEE0327\", \"fee_amount\" : 255, "
                    + "\"description\" : \"example\", \"version\" : 1 }", APPLICATION_JSON));

            List<FeeResponse> fees = feeService.getFees(List.of(feeType));
            FeeResponse actual = fees.get(0);

            assertThat(fees).hasSize(1);
            assertThat(actual.getCode()).isEqualTo("FEE0327");
            assertThat(actual.getDescription()).isEqualTo("example");
            assertThat(actual.getVersion()).isEqualTo(1);
            assertThat(actual.getAmount()).isEqualTo(BigDecimal.valueOf(255));
        }

        @Test
        void shouldFilterOutNullValuesWhenThereIsAnErrorInTheResponse() {
            mockServer.expect(requestTo(buildURI()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withBadRequest());

            List<FeeResponse> feeResponses = feeService.getFees(List.of(CARE_ORDER));

            assertThat(feeResponses).isEmpty();
        }

        private String buildURI() {
            return "http://localhost:8080/fees-register/fees/lookup?service=private%20law&jurisdiction1=family" +
                "&jurisdiction2=family%20court&channel=default&event=miscellaneous&keyword=KLM";
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
