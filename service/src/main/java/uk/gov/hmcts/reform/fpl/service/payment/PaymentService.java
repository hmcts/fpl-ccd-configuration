package uk.gov.hmcts.reform.fpl.service.payment;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fnp.client.PaymentApi;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fnp.model.payment.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.request.RequestData;

import java.math.BigDecimal;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.fnp.model.payment.enums.Currency.GBP;
import static uk.gov.hmcts.reform.fnp.model.payment.enums.Service.FPL;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@Slf4j
public class PaymentService {

    private static final String DESCRIPTION_TEMPLATE = "Payment for case: %s";
    private static final String BLANK_CUSTOMER_REFERENCE_VALUE = "Not provided";

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

    public void makePaymentForCaseOrders(Long caseId, CaseData caseData) {
        FeesData feesData = feeService.getFeesDataForOrders(caseData.getOrders());

        if (!feesData.getTotalAmount().equals(BigDecimal.ZERO)) {
            String localAuthorityName =
                localAuthorityNameLookupConfiguration.getLocalAuthorityName(caseData.getCaseLocalAuthority());
            ApplicantParty applicantParty = getFirstApplicantParty(caseData);
            CreditAccountPaymentRequest paymentRequest = getCreditAccountPaymentRequest(caseId,
                applicantParty.getPbaNumber(),
                applicantParty.getClientCode(),
                defaultCustomerReferenceIfBlank(applicantParty.getCustomerReference()),
                localAuthorityName,
                feesData);

            callPaymentsApi(paymentRequest);
        }
    }

    private ApplicantParty getFirstApplicantParty(CaseData caseData) {
        return caseData.getApplicants().get(0).getValue().getParty();
    }

    public void makePaymentForC2(Long caseId, CaseData caseData) {
        C2DocumentBundle c2DocumentBundle = getLastC2DocumentBundle(caseData);
        String localAuthorityName =
            localAuthorityNameLookupConfiguration.getLocalAuthorityName(caseData.getCaseLocalAuthority());
        FeesData feesData = feeService.getFeesDataForC2(c2DocumentBundle.getType());

        CreditAccountPaymentRequest paymentRequest = getCreditAccountPaymentRequest(caseId,
            c2DocumentBundle.getPbaNumber(),
            c2DocumentBundle.getClientCode(),
            defaultCustomerReferenceIfBlank(c2DocumentBundle.getFileReference()),
            localAuthorityName,
            feesData);

        callPaymentsApi(paymentRequest);
    }

    private String defaultCustomerReferenceIfBlank(final String currentValue) {
        return isNotBlank(currentValue) ? currentValue : BLANK_CUSTOMER_REFERENCE_VALUE;
    }

    private CreditAccountPaymentRequest getCreditAccountPaymentRequest(Long caseId, String pbaNumber,
                                                                       String caseReference,
                                                                       String customerReference,
                                                                       String localAuthorityName,
                                                                       FeesData feesData) {
        return CreditAccountPaymentRequest.builder()
            .accountNumber(pbaNumber)
            .amount(feesData.getTotalAmount())
            .caseReference(caseReference)
            .ccdCaseNumber(String.valueOf(caseId))
            .currency(GBP)
            .customerReference(customerReference)
            .description(String.format(DESCRIPTION_TEMPLATE, caseId))
            .organisationName(localAuthorityName)
            .service(FPL)
            .siteId(siteId)
            .fees(feesData.getFees())
            .build();
    }

    private C2DocumentBundle getLastC2DocumentBundle(CaseData caseData) {
        var c2DocumentBundle = unwrapElements(caseData.getC2DocumentBundle());

        return c2DocumentBundle.get(c2DocumentBundle.size() - 1);
    }

    private void callPaymentsApi(CreditAccountPaymentRequest creditAccountPaymentRequest) {
        try {
            paymentApi.createCreditAccountPayment(requestData.authorisation(),
                authTokenGenerator.generate(),
                creditAccountPaymentRequest);
        } catch (FeignException ex) {
            log.error("Payments response error for {}\n\tstatus: {} => message: \"{}\"",
                creditAccountPaymentRequest, ex.status(), ex.contentUTF8(), ex);

            throw new PaymentsApiException(ex.status(), ex.contentUTF8(), ex);
        }
    }
}
