package uk.gov.hmcts.reform.fnp.model.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fnp.model.fee.FeeResponse;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class FeeDto {
    @JsonProperty("calculated_amount")
    private BigDecimal calculatedAmount;
    private Integer version;
    private String code;

    public static FeeDto fromFeeResponse(FeeResponse feeResponse) {
        return FeeDto.builder()
            .calculatedAmount(feeResponse.getAmount())
            .version(feeResponse.getVersion())
            .code(feeResponse.getCode())
            .build();
    }
}
