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
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;

import java.math.BigDecimal;

import static uk.gov.hmcts.reform.fnp.model.payment.enums.Currency.GBP;
import static uk.gov.hmcts.reform.fnp.model.payment.enums.Service.FPL;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
public class PaymentService {

    private static final String DESCRIPTION_TEMPLATE = "Payment for case: %s";

    private final FeeService feeService;
    private final PaymentApi paymentApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final RequestData requestData;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final String siteId;

    @Autowired
    public PaymentService(FeeService feeService, PaymentApi paymentApi, AuthTokenGenerator authTokenGenerator,
                          RequestData requestData,
                          LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration,
                          @Value("${payment.site_id}") String siteId) {
        this.feeService = feeService;
        this.paymentApi = paymentApi;
        this.authTokenGenerator = authTokenGenerator;
        this.requestData = requestData;
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
        this.siteId = siteId;
    }

    public void makePaymentForCase(Long caseId, CaseData caseData) {
        FeesData feesData = feeService.getFeesDataForOrders(caseData.getOrders());
        String localAuthorityName =
            localAuthorityNameLookupConfiguration.getLocalAuthorityName(caseData.getCaseLocalAuthority());
        String pbaNumber = "?"; //TODO:
        String clientCode = "?"; //TODO:

        if (!feesData.getTotalAmount().equals(BigDecimal.ZERO)) {
            CreditAccountPaymentRequest paymentRequest = getCreditAccountPaymentRequest(caseId,
                pbaNumber,
                clientCode,
                localAuthorityName,
                feesData);


            paymentApi.createCreditAccountPayment(requestData.authorisation(),
                authTokenGenerator.generate(),
                paymentRequest);
        }
    }

    public void makePaymentForC2(Long caseId, CaseData caseData) {
        C2DocumentBundle c2DocumentBundle = getLastC2DocumentBundle(caseData);
        String localAuthorityName =
            localAuthorityNameLookupConfiguration.getLocalAuthorityName(caseData.getCaseLocalAuthority());
        FeesData feesData = feeService.getFeesDataForC2(c2DocumentBundle.getType());

        CreditAccountPaymentRequest paymentRequest = getCreditAccountPaymentRequest(caseId,
            c2DocumentBundle.getPbaNumber(),
            c2DocumentBundle.getClientCode(),
            localAuthorityName,
            feesData);

        paymentApi.createCreditAccountPayment(requestData.authorisation(),
            authTokenGenerator.generate(),
            paymentRequest);
    }

    private CreditAccountPaymentRequest getCreditAccountPaymentRequest(Long caseId, String pbaNumber,
                                                                       String customerReference,
                                                                       String localAuthorityName,
                                                                       FeesData feesData) {
        return CreditAccountPaymentRequest.builder()
            .accountNumber(pbaNumber)
            .amount(feesData.getTotalAmount().doubleValue())
            .caseReference(String.valueOf(caseId))
            .ccdCaseNumber(String.valueOf(caseId))
            .currency(GBP)
            .customerReference(customerReference)
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
}
