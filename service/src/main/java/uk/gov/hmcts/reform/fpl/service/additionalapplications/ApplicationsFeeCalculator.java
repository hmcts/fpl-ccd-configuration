package uk.gov.hmcts.reform.fpl.service.additionalapplications;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;
import uk.gov.hmcts.reform.fpl.utils.BigDecimalHelper;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicationsFeeCalculator {

    private static final String DISPLAY_AMOUNT_TO_PAY = "displayAmountToPay";
    private static final String AMOUNT_TO_PAY = "amountToPay";

    private final FeeService feeService;

    public void getC2ApplicationFee(Map<String, Object> data, CaseData caseData) {
        try {
            FeesData feesData = feeService.getFeesDataForC2(caseData.getC2ApplicationType().get("type"));
            data.put(AMOUNT_TO_PAY, BigDecimalHelper.toCCDMoneyGBP(feesData.getTotalAmount()));
            data.put(DISPLAY_AMOUNT_TO_PAY, YES.getValue());
        } catch (FeeRegisterException ignore) {
            data.put(DISPLAY_AMOUNT_TO_PAY, NO.getValue());
        }
    }

    public void getOtherApplicationsFee(Map<String, Object> data, CaseData caseData) {
        try {
            FeesData feesData = feeService.getFeesDataForOtherApplications(
                caseData.getTemporaryOtherApplicationsBundle().getApplicationType(),
                caseData.getTemporaryOtherApplicationsBundle().getSupplementsTypes());

            data.put(AMOUNT_TO_PAY, BigDecimalHelper.toCCDMoneyGBP(feesData.getTotalAmount()));
            data.put(DISPLAY_AMOUNT_TO_PAY, YES.getValue());
        } catch (FeeRegisterException ignore) {
            data.put(DISPLAY_AMOUNT_TO_PAY, NO.getValue());
        }
    }

    public void getAdditionalApplicationsFee(Map<String, Object> data, CaseData caseData) {
        try {
            FeesData feesData = feeService.getFeesDataForAdditionalApplications(
                caseData.getC2ApplicationType().get("type"),
                caseData.getTemporaryOtherApplicationsBundle().getApplicationType(),
                caseData.getTemporaryOtherApplicationsBundle().getSupplementsTypes());

            data.put(AMOUNT_TO_PAY, BigDecimalHelper.toCCDMoneyGBP(feesData.getTotalAmount()));
            data.put(DISPLAY_AMOUNT_TO_PAY, YES.getValue());
        } catch (FeeRegisterException ignore) {
            data.put(DISPLAY_AMOUNT_TO_PAY, NO.getValue());
        }
    }
}
