package uk.gov.hmcts.reform.fnp.model.payment;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class Payment {

    private BigDecimal amount;

    @JsonProperty("account_number")
    @JsonAlias({"accountNumber"})
    private String accountNumber;
}
