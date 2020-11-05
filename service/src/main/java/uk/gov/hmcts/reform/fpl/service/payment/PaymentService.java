package uk.gov.hmcts.reform.fpl.service.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fnp.model.payment.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;

import java.math.BigDecimal;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static uk.gov.hmcts.reform.fnp.model.payment.enums.Currency.GBP;
import static uk.gov.hmcts.reform.fnp.model.payment.enums.Service.FPL;

@Service
@Slf4j
public class PaymentService {

    private static final String DESCRIPTION_TEMPLATE = "Payment for case: %s";
    public static final String BLANK_PARAMETER_VALUE = "Not provided";

    private final FeeService feeService;
    private final PaymentClient paymentClient;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final String siteId;

    @Autowired
    public PaymentService(FeeService feeService, PaymentClient paymentClient,
                          LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration,
                          @Value("${payment.site_id}") String siteId) {
        this.feeService = feeService;
        this.paymentClient = paymentClient;
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
        this.siteId = siteId;
    }

    public void makePaymentForCaseOrders(CaseData caseData) {
        FeesData feesData = feeService.getFeesDataForOrders(caseData.getOrders());

        if (!feesData.getTotalAmount().equals(BigDecimal.ZERO)) {
            String localAuthorityName =
                localAuthorityNameLookupConfiguration.getLocalAuthorityName(caseData.getCaseLocalAuthority());
            ApplicantParty applicantParty = getFirstApplicantParty(caseData);

            CreditAccountPaymentRequest paymentRequest = getCreditAccountPaymentRequest(caseData.getId(),
                applicantParty.getPbaNumber(),
                defaultIfBlank(applicantParty.getClientCode(), BLANK_PARAMETER_VALUE),
                defaultIfBlank(applicantParty.getCustomerReference(), BLANK_PARAMETER_VALUE),
                localAuthorityName,
                feesData);

            callPaymentsApi(paymentRequest);
        }
    }

    private ApplicantParty getFirstApplicantParty(CaseData caseData) {
        return caseData.getApplicants().get(0).getValue().getParty();
    }

    public void makePaymentForC2(Long caseId, CaseData caseData) {
        C2DocumentBundle c2DocumentBundle = caseData.getLastC2DocumentBundle();
        String localAuthorityName =
            localAuthorityNameLookupConfiguration.getLocalAuthorityName(caseData.getCaseLocalAuthority());
        FeesData feesData = feeService.getFeesDataForC2(c2DocumentBundle.getType());

        CreditAccountPaymentRequest paymentRequest = getCreditAccountPaymentRequest(caseId,
            c2DocumentBundle.getPbaNumber(),
            defaultIfBlank(c2DocumentBundle.getClientCode(), BLANK_PARAMETER_VALUE),
            defaultIfBlank(c2DocumentBundle.getFileReference(), BLANK_PARAMETER_VALUE),
            localAuthorityName,
            feesData);

        callPaymentsApi(paymentRequest);
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

    // callPaymentsApi has been called from PaymentClient because of springboot limitations on retry mechanism
    public void callPaymentsApi(CreditAccountPaymentRequest creditAccountPaymentRequest) {
        paymentClient.callPaymentsApi(creditAccountPaymentRequest);
    }
}
