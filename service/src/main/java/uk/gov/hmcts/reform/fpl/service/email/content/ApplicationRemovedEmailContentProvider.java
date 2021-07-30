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
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ApplicationRemovedEmailContentProvider extends AbstractEmailContentProvider {
    private final EmailNotificationHelper helper;
    private final RemoveApplicationService removeApplicationService;

    public ApplicationRemovedNotifyData getNotifyData(final CaseData caseData,
                                                      final AdditionalApplicationsBundle removedApplication) {
        return ApplicationRemovedNotifyData.builder()
            .childLastName(helper.getEldestChildLastName(caseData.getAllChildren()))
            .caseId(caseData.getId().toString())
            .c2Filename(removeApplicationService.getFilename(removedApplication))
            .removalDate(formatLocalDateTimeBaseUsingFormat(LocalDateTime.now(), DATE_TIME_AT))
            .reason(removeApplicationService.getRemovalReason(removedApplication.getRemovalReason()))
            .applicantName(removeApplicationService.getApplicantName(removedApplication))
            .applicationFeeText(removeApplicationService.getApplicationFee(removedApplication))
            .caseUrl(getCaseUrl(caseData.getId()))
            .build();
    }
}
