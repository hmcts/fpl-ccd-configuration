package uk.gov.hmcts.reform.fpl.utils;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.RemovalReason;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.RemovalReason.*;
import static uk.gov.hmcts.reform.fpl.utils.BigDecimalHelper.fromCCDMoneyGBP;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RemovedApplicationNotificationHelper {

    public String getApplicationFee(AdditionalApplicationsBundle removedApplication) {
        String fee = removedApplication.getAmountToPay();
        Optional<BigDecimal> decimalAmount = fromCCDMoneyGBP(fee);
        String refundFeeText;

        if(decimalAmount.isPresent()) {
            BigDecimal amountToDisplay = decimalAmount.get();
            refundFeeText = "An application fee of £" + amountToDisplay + " needs to be refunded.";
        } else {
            refundFeeText = "An application fee needs to be refunded.";
        }

        return refundFeeText;
    }

    public String getApplicantName(AdditionalApplicationsBundle removedApplication) {
        if(!isEmpty(removedApplication.getC2DocumentBundle())) {
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
        if(!isEmpty(removedApplication.getC2DocumentBundle())) {
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
        if(removalReason.equals(DUPLICATE)) {
            return DUPLICATE.getLabel();
        } else if(removalReason.equals(WRONG_CASE)) {
            return WRONG_CASE.getLabel();
        }
        return removalReason;
    }
}
