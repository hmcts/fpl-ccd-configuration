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
import uk.gov.hmcts.reform.fpl.utils.RemovedApplicationNotificationHelper;

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
    private final RemovedApplicationNotificationHelper removedApplicationHelper;

    public ApplicationRemovedNotifyData getNotifyData(final CaseData caseData, final AdditionalApplicationsBundle removedApplication) {
        return ApplicationRemovedNotifyData.builder()
            .childLastName(helper.getEldestChildLastName(caseData.getAllChildren()))
            .caseId(caseData.getId().toString())
            .c2Filename(removedApplicationHelper.getFilename(removedApplication))
            .removalDate(formatLocalDateTimeBaseUsingFormat(LocalDateTime.now(), DATE_TIME_AT))
            .reason(removedApplication.getRemovalReason().toLowerCase())
            .applicantName(removedApplicationHelper.getApplicantName(removedApplication))
            .applicationFeeText(removedApplicationHelper.getApplicationFee(removedApplication))
            .caseUrl(getCaseUrl(caseData.getId()))
            .build();
    }
}
