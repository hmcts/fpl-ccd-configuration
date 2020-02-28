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

import java.math.BigDecimal;
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
        Double totalFeeAmount = BigDecimal.ZERO.doubleValue(); //TODO: store and get from caseData
        List<FeeDto> fees = List.of(FeeDto.builder().build());

        CreditAccountPaymentRequest paymentRequest = CreditAccountPaymentRequest.builder()
            .accountNumber("PBA0082848") //TODO: test PBA number, replace with real one
            .amount(totalFeeAmount)
            .caseReference(String.valueOf(caseId))
            .currency(GBP)
            .customerReference("TBC") //TODO: take from c2 screen?
            .description("FREETEXT") //TODO: order type: EPO, Care Order...
            .organisationName("FREETEXT") //TODO: Local Authority name
            .service(FPL)
            .siteId(SITE_ID)
            .fees(fees)
            .build();

        paymentApi.createCreditAccountPayment(requestData.authorisation(),
            authTokenGenerator.generate(),
            paymentRequest);
    }
}
