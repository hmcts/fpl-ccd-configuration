package uk.gov.hmcts.reform.fnp.model.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fnp.model.payment.enums.Currency;
import uk.gov.hmcts.reform.fnp.model.payment.enums.Service;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class CreditAccountPaymentRequest {

    private String accountNumber;
    private double amount;
    private String caseReference;
    private String ccdCaseNumber;
    private Currency currency;
    private String customerReference;
    private String description;
    private String organisationName;
    private Service service;
    private String siteId;
    private List<FeeDto> fees;
}
