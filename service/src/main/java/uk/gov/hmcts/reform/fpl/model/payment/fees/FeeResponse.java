package uk.gov.hmcts.reform.fpl.model.payment.fees;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FeeResponse {
    private final String code;
    private final String description;
    private final String version;
    @JsonProperty(value = "fee_amount")
    private final BigDecimal amount;
}
