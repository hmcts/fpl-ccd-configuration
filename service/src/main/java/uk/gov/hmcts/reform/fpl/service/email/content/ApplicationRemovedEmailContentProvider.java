package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.notify.ApplicationRemovedNotifyData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.removeorder.RemoveApplicationService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import static uk.gov.hmcts.reform.fpl.model.notify.ApplicationRemovedNotifyData.builder;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ApplicationRemovedEmailContentProvider extends AbstractEmailContentProvider {
    private final EmailNotificationHelper helper;
    private final RemoveApplicationService removeApplicationService;
    private final Time time;

    public ApplicationRemovedNotifyData getNotifyData(final CaseData caseData,
                                                      final AdditionalApplicationsBundle removedApplication) {
        return builder()
            .childLastName(helper.getEldestChildLastName(caseData.getAllChildren()))
            .caseId(caseData.getId().toString())
            .c2Filename(removeApplicationService.getFilename(removedApplication))
            .removalDate(formatLocalDateTimeBaseUsingFormat(time.now(), DATE_TIME_AT))
            .reason(removedApplication.getRemovalReason())
            .applicantName(removeApplicationService.getApplicantName(removedApplication))
            .applicationFeeText(removeApplicationService.getApplicationFee(removedApplication))
            .caseUrl(getCaseUrl(caseData.getId()))
            .build();
    }
}
