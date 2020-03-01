package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.payment.client.PaymentApi;
import uk.gov.hmcts.reform.payment.model.CreditAccountPaymentRequest;

import java.math.BigDecimal;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
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
        FeesData feesData = caseData.getFeesData();

        CreditAccountPaymentRequest paymentRequest = CreditAccountPaymentRequest.builder()
            .accountNumber("PBA0082848") //TODO: test PBA number, take from (sth/c2Data/caseData)
            .amount(feesData.getTotalAmount().doubleValue())
            .caseReference(String.valueOf(caseId))
            .currency(GBP)
            .customerReference("TBC") //TODO: take from c2 screen?
            .description("FREETEXT") //TODO: order type: EPO, Care Order... -> single value for multiple fees
            .organisationName("FREETEXT") //TODO: Local Authority name -> from RequestData? (remove inject from controller)
            .service(FPL)
            .siteId(SITE_ID)
            .fees(unwrapElements(feesData.getFees()))
            .build();

        //TODO: logging / error handling here (controller doesn't care)
        paymentApi.createCreditAccountPayment(requestData.authorisation(),
            authTokenGenerator.generate(),
            paymentRequest);
    }
}
