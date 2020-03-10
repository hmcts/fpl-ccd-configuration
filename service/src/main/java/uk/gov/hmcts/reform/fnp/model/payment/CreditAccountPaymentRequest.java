package uk.gov.hmcts.reform.fnp.model.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fnp.model.payment.enums.Currency;
import uk.gov.hmcts.reform.fnp.model.payment.enums.Service;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class CreditAccountPaymentRequest {
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
}
