package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.notify.ApplicationRemovedNotifyData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.utils.BigDecimalHelper.fromCCDMoneyGBP;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ApplicationRemovedEmailContentProvider extends AbstractEmailContentProvider {
    private final EmailNotificationHelper helper;

    public ApplicationRemovedNotifyData getNotifyData(final CaseData caseData, final AdditionalApplicationsBundle removedApplication) {
        return ApplicationRemovedNotifyData.builder()
            .childLastName(helper.getEldestChildLastName(caseData.getAllChildren()))
            .caseId(caseData.getId().toString())
            .c2Filename(getFilename(removedApplication))
            .removalDate(formatLocalDateTimeBaseUsingFormat(LocalDateTime.now(), DATE_TIME_AT))
            .reason(removedApplication.getRemovalReason().toLowerCase())
            .applicantName(getApplicantName(removedApplication))
            .applicationFeeText(getApplicationFee(removedApplication))
            .build();
    }

    private String getApplicationFee(AdditionalApplicationsBundle removedApplication) {
        String fee = removedApplication.getAmountToPay();
        Optional<BigDecimal> decimalAmount = fromCCDMoneyGBP(fee);
        String refundFeeText;

        if(decimalAmount.isPresent()) {
            BigDecimal amountToDisplay = decimalAmount.get();
            refundFeeText = "An application fee of £" + amountToDisplay + "needs to be refunded.";
        } else {
            refundFeeText = "An application fee needs to be refunded.";
        }

        return refundFeeText;
    }

    private String getApplicantName(AdditionalApplicationsBundle removedApplication) {
        if(!isEmpty(removedApplication.getC2DocumentBundle())) {
           return removedApplication.getC2DocumentBundle().getApplicantName();
        }

        if (!isEmpty(removedApplication.getOtherApplicationsBundle())) {
           return removedApplication.getOtherApplicationsBundle().getApplicantName();
        }

        return "";
    }

    private String getFilename(AdditionalApplicationsBundle removedApplication) {
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
}
