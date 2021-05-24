package uk.gov.hmcts.reform.fpl.service.email.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.PLACEMENT;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Component
public class LocalAuthorityEmailContentProvider extends AbstractEmailContentProvider {
    public BaseCaseNotifyData buildNoticeOfPlacementOrderUploadedNotification(CaseData caseData) {
        return BaseCaseNotifyData.builder()
            .respondentLastName(getFirstRespondentLastName(caseData))
            .caseUrl(getCaseUrl(caseData.getId(), PLACEMENT))
            .build();
    }
}
