package uk.gov.hmcts.reform.fnp.model.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import uk.gov.hmcts.reform.fnp.model.payment.enums.Currency;
import uk.gov.hmcts.reform.fnp.model.payment.enums.Service;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreditAccountPaymentRequest {
    @ToString.Exclude
    @JsonProperty("account_number")
    private String accountNumber;
    @JsonProperty("amount")
    private BigDecimal amount;
    @JsonProperty("case_reference")
    private String caseReference;
    @JsonProperty("ccd_case_number")
    private String ccdCaseNumber;
    @JsonProperty("currency")
    private Currency currency;
    @JsonProperty("customer_reference")
    private String customerReference;
    @JsonProperty("description")
    private String description;
    @JsonProperty("organisation_name")
    private String organisationName;
    @JsonProperty("service")
    private Service service;
    @JsonProperty("site_id")
    private String siteId;
    @JsonProperty("fees")
    private List<FeeDto> fees;
    @JsonProperty("case_type")
    private String caseType;
}
