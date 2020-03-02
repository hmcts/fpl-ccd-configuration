package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fnp.model.fee.FeeResponse;
import uk.gov.hmcts.reform.payment.model.FeeDto;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FeeDtoTest {

    @Test
    void shouldMapFeeResponseToFeeDto() {
        FeeResponse feeResponse = new FeeResponse();
        feeResponse.setCode("test code");
        feeResponse.setDescription("test description");
        feeResponse.setVersion(5);
        feeResponse.setAmount(BigDecimal.ONE);

        FeeDto result = FeeDto.fromFeeResponse(feeResponse);

        assertThat(result.getCalculatedAmount()).isEqualTo(feeResponse.getAmount());
        assertThat(result.getVersion()).isEqualTo(feeResponse.getVersion());
        assertThat(result.getCode()).isEqualTo(feeResponse.getCode());
    }
}
