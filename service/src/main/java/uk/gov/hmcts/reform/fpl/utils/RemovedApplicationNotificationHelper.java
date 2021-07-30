package uk.gov.hmcts.reform.fpl.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.RemovalReason.DUPLICATE;
import static uk.gov.hmcts.reform.fpl.enums.RemovalReason.WRONG_CASE;
import static uk.gov.hmcts.reform.fpl.utils.BigDecimalHelper.fromCCDMoneyGBP;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RemovedApplicationNotificationHelper {

    public String getApplicationFee(AdditionalApplicationsBundle removedApplication) {
        String fee = removedApplication.getAmountToPay();
        Optional<BigDecimal> decimalAmount = fromCCDMoneyGBP(fee);
        String refundFeeText;

        if (decimalAmount.isPresent()) {
            BigDecimal amountToDisplay = decimalAmount.get();
            refundFeeText = "An application fee of £" + amountToDisplay + " needs to be refunded.";
        } else {
            refundFeeText = "An application fee needs to be refunded.";
        }

        return refundFeeText;
    }

    public String getApplicantName(AdditionalApplicationsBundle removedApplication) {
        if (!isEmpty(removedApplication.getC2DocumentBundle())) {
            return removedApplication.getC2DocumentBundle().getApplicantName();
        }

        if (!isEmpty(removedApplication.getOtherApplicationsBundle())) {
            return removedApplication.getOtherApplicationsBundle().getApplicantName();
        }

        return "";
    }

    public String getFilename(AdditionalApplicationsBundle removedApplication) {
        String c2DocumentName = "";
        String otherDocumentName = "";
        if (!isEmpty(removedApplication.getC2DocumentBundle())) {
            c2DocumentName = removedApplication.getC2DocumentBundle().getDocument().getFilename();
        }

        if (!isEmpty(removedApplication.getOtherApplicationsBundle())) {
            otherDocumentName = removedApplication.getOtherApplicationsBundle().getDocument().getFilename();
        }

        return Stream.of(c2DocumentName, otherDocumentName)
            .filter(s -> s != null && !s.isEmpty())
            .collect(Collectors.joining(", "));
    }

    public String getRemovalReason(String removalReason) {
        if (removalReason.equals(DUPLICATE)) {
            return DUPLICATE.getLabel();
        } else if (removalReason.equals(WRONG_CASE)) {
            return WRONG_CASE.getLabel();
        }
        return removalReason;
    }
}
