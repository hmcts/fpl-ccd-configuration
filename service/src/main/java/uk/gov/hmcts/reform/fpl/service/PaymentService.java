package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.payment.client.PaymentApi;
import uk.gov.hmcts.reform.payment.model.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.payment.model.FeeDto;

import java.util.List;

import static uk.gov.hmcts.reform.payment.model.enums.Currency.GBP;
import static uk.gov.hmcts.reform.payment.model.enums.Service.FPL;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PaymentService {

    private static final String SITE_ID = "ABA3";

    private final PaymentApi paymentApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final RequestData requestData;

    public void makePayment(Long caseId, CaseData caseData) {
        List<FeeDto> fees = List.of(FeeDto.builder().build());

        CreditAccountPaymentRequest paymentRequest = CreditAccountPaymentRequest.builder()
            .accountNumber("PBA0082848") //TODO: test PBA number, replace with real one
            .amount(getSumOfFees(fees))
            .caseReference(String.valueOf(caseId))
            .currency(GBP)
            .customerReference("TBC") //TODO
            .description("FREETEXT") //TODO
            .organisationName("FREETEXT") //TODO
            .service(FPL) // TODO: FPL not added yet (enum from Payments)
            .siteId(SITE_ID)
            .fees(fees)
            .build();

        paymentApi.createCreditAccountPayment(requestData.authorisation(),
            authTokenGenerator.generate(),
            paymentRequest);
    }

    private double getSumOfFees(List<FeeDto> fees) {
        return fees.stream().map(FeeDto::getCalculatedAmount).mapToDouble(Double::doubleValue).sum();
    }

}
