package uk.gov.hmcts.reform.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.payment.model.enums.Currency;
import uk.gov.hmcts.reform.payment.model.enums.Service;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditAccountPaymentRequest {

    private String accountNumber;
    private double amount;
    private String caseReference;
    private String ccdCaseNumber; // optional
    private Currency currency;
    private String customerReference;
    private String description;
    private String organisationName;
    private Service service;
    private String siteId;
    private List<FeeDto> fees;
}
