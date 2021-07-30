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
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import java.math.BigDecimal;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static uk.gov.hmcts.reform.fnp.model.payment.enums.Currency.GBP;
import static uk.gov.hmcts.reform.fnp.model.payment.enums.Service.FPL;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;

@Service
@Slf4j
public class PaymentService {

    private static final String DESCRIPTION_TEMPLATE = "Payment for case: %s";
    public static final String BLANK_PARAMETER_VALUE = "Not provided";

    private final FeeService feeService;
    private final FeatureToggleService featureToggleService;
    private final PaymentClient paymentClient;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final String siteId;

    @Autowired
    public PaymentService(FeeService feeService,
                          FeatureToggleService featureToggleService,
                          PaymentClient paymentClient,
                          LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration,
                          @Value("${payment.site_id}") String siteId) {
        this.feeService = feeService;
        this.featureToggleService = featureToggleService;
        this.paymentClient = paymentClient;
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
        this.siteId = siteId;
    }

    public void makePaymentForCaseOrders(CaseData caseData) {
        FeesData feesData = feeService.getFeesDataForOrders(caseData.getOrders());

        if (!feesData.getTotalAmount().equals(BigDecimal.ZERO)) {
            final CreditAccountPaymentRequest paymentRequest = getPaymentRequest(caseData, feesData);

            paymentClient.callPaymentsApi(paymentRequest);
        }
    }

    private CreditAccountPaymentRequest getPaymentRequest(CaseData caseData, FeesData feesData) {
        final String localAuthorityName =
            localAuthorityNameLookupConfiguration.getLocalAuthorityName(caseData.getCaseLocalAuthority());

        if (isNotEmpty(caseData.getLocalAuthorities())) {
            final LocalAuthority localAuthority = caseData.getLocalAuthorities().get(0).getValue();

            return getCreditAccountPaymentRequest(caseData.getId(),
                localAuthority.getPbaNumber(),
                defaultIfBlank(localAuthority.getClientCode(), BLANK_PARAMETER_VALUE),
                defaultIfBlank(localAuthority.getCustomerReference(), BLANK_PARAMETER_VALUE),
                localAuthorityName,
                feesData);
        }

        final ApplicantParty legacyApplicant = caseData.getApplicants().get(0).getValue().getParty();

        return getCreditAccountPaymentRequest(caseData.getId(),
            legacyApplicant.getPbaNumber(),
            defaultIfBlank(legacyApplicant.getClientCode(), BLANK_PARAMETER_VALUE),
            defaultIfBlank(legacyApplicant.getCustomerReference(), BLANK_PARAMETER_VALUE),
            localAuthorityName,
            feesData);
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

        paymentClient.callPaymentsApi(paymentRequest);
    }

    public void makePaymentForAdditionalApplications(Long caseId, CaseData caseData, FeesData feesData) {
        final PBAPayment pbaPayment = caseData.getAdditionalApplicationsBundle().get(0).getValue().getPbaPayment();

        String localAuthorityName =
            localAuthorityNameLookupConfiguration.getLocalAuthorityName(caseData.getCaseLocalAuthority());

        CreditAccountPaymentRequest paymentRequest = getCreditAccountPaymentRequest(caseId,
            pbaPayment.getPbaNumber(),
            defaultIfBlank(pbaPayment.getClientCode(), BLANK_PARAMETER_VALUE),
            defaultIfBlank(pbaPayment.getFileReference(), BLANK_PARAMETER_VALUE),
            localAuthorityName,
            feesData);

        paymentClient.callPaymentsApi(paymentRequest);
    }

    private CreditAccountPaymentRequest getCreditAccountPaymentRequest(Long caseId, String pbaNumber,
                                                                       String caseReference,
                                                                       String customerReference,
                                                                       String localAuthorityName,
                                                                       FeesData feesData) {
        CreditAccountPaymentRequest.CreditAccountPaymentRequestBuilder builder = CreditAccountPaymentRequest.builder()
            .accountNumber(pbaNumber)
            .amount(feesData.getTotalAmount())
            .caseReference(caseReference)
            .ccdCaseNumber(String.valueOf(caseId))
            .currency(GBP)
            .customerReference(customerReference)
            .description(String.format(DESCRIPTION_TEMPLATE, caseId))
            .organisationName(localAuthorityName)
            .service(FPL)
            .fees(feesData.getFees());

        if (featureToggleService.isFeeAndPayCaseTypeEnabled()) {
            builder.caseType(CASE_TYPE);
        } else {
            builder.siteId(siteId);
        }

        return builder.build();
    }
}
