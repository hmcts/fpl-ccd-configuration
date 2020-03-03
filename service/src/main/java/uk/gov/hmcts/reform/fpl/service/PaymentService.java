package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fnp.client.PaymentApi;
import uk.gov.hmcts.reform.fnp.model.payment.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.request.RequestData;

import java.math.BigDecimal;

import static uk.gov.hmcts.reform.fnp.model.payment.enums.Currency.GBP;
import static uk.gov.hmcts.reform.fnp.model.payment.enums.Service.FPL;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
public class PaymentService {

    private static final String DESCRIPTION_TEMPLATE = "Payment for case: %s";

    private final PaymentApi paymentApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final RequestData requestData;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final String siteId;

    @Autowired
    public PaymentService(PaymentApi paymentApi, AuthTokenGenerator authTokenGenerator, RequestData requestData,
                          LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration,
                          @Value("${payment.site_id}") String siteId) {
        this.paymentApi = paymentApi;
        this.authTokenGenerator = authTokenGenerator;
        this.requestData = requestData;
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
        this.siteId = siteId;
    }

    public void makePayment(Long caseId, CaseData caseData) {
        if (shouldMakePayment(caseData)) {
            CreditAccountPaymentRequest paymentRequest = getCreditAccountPaymentRequest(caseId, caseData);

            paymentApi.createCreditAccountPayment(requestData.authorisation(),
                authTokenGenerator.generate(),
                paymentRequest);
        }
    }

    private CreditAccountPaymentRequest getCreditAccountPaymentRequest(Long caseId, CaseData caseData) {
        FeesData feesData = caseData.getFeesData();
        String localAuthorityName =
            localAuthorityNameLookupConfiguration.getLocalAuthorityName(caseData.getCaseLocalAuthority());
        C2DocumentBundle c2DocumentBundle = getLastC2DocumentBundle(caseData);

        return CreditAccountPaymentRequest.builder()
            .accountNumber(c2DocumentBundle.getPbaNumber())
            .amount(feesData.getTotalAmount().doubleValue())
            .caseReference(String.valueOf(caseId))
            .ccdCaseNumber(String.valueOf(caseId))
            .currency(GBP)
            .customerReference(c2DocumentBundle.getClientCode())
            .description(String.format(DESCRIPTION_TEMPLATE, caseId))
            .organisationName(localAuthorityName)
            .service(FPL)
            .siteId(siteId)
            .fees(unwrapElements(feesData.getFees()))
            .build();
    }

    private C2DocumentBundle getLastC2DocumentBundle(CaseData caseData) {
        var c2DocumentBundle = unwrapElements(caseData.getC2DocumentBundle());

        return c2DocumentBundle.get(c2DocumentBundle.size() - 1);
    }

    private boolean shouldMakePayment(CaseData caseData) {
        return !caseData.getFeesData().getTotalAmount().equals(BigDecimal.ZERO);
    }
}
