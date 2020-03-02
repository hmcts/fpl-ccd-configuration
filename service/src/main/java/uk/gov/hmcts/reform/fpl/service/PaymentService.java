package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.payment.client.PaymentApi;
import uk.gov.hmcts.reform.payment.model.CreditAccountPaymentRequest;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.payment.model.enums.Currency.GBP;
import static uk.gov.hmcts.reform.payment.model.enums.Service.FPL;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PaymentService {

    private static final String SITE_ID = "ABA3";
    private static final String DESCRIPTION_TEMPLATE = "Payment for case %s";

    private final PaymentApi paymentApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final RequestData requestData;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;

    public void makePayment(Long caseId, CaseData caseData) {
        CreditAccountPaymentRequest paymentRequest = getCreditAccountPaymentRequest(caseId, caseData);

        paymentApi.createCreditAccountPayment(requestData.authorisation(),
            authTokenGenerator.generate(),
            paymentRequest);
    }

    private CreditAccountPaymentRequest getCreditAccountPaymentRequest(Long caseId, CaseData caseData) {
        FeesData feesData = caseData.getFeesData();
        String localAuthorityName = localAuthorityNameLookupConfiguration.getLocalAuthorityName(caseData.getCaseLocalAuthority());
        C2DocumentBundle c2DocumentBundle = getLastC2DocumentBundle(caseData);

        return CreditAccountPaymentRequest.builder()
            .accountNumber(c2DocumentBundle.getPbaNumber())
            .amount(feesData.getTotalAmount().doubleValue())
            .caseReference(String.valueOf(caseId))
            .currency(GBP)
            .customerReference(c2DocumentBundle.getClientCode())
            .description(String.format(DESCRIPTION_TEMPLATE, caseId))
            .organisationName(localAuthorityName)
            .service(FPL)
            .siteId(SITE_ID)
            .fees(unwrapElements(feesData.getFees()))
            .build();
    }

    private C2DocumentBundle getLastC2DocumentBundle(CaseData caseData) {
        var c2DocumentBundle = unwrapElements(caseData.getC2DocumentBundle());

        return c2DocumentBundle.get(c2DocumentBundle.size() - 1);
    }
}
