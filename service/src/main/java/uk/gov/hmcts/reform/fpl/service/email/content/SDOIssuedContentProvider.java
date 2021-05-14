package uk.gov.hmcts.reform.fpl.service.email.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.SDONotifyData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.ORDERS;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCallout;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Component
public class SDOIssuedContentProvider extends AbstractEmailContentProvider {
    public SDONotifyData buildNotificationParameters(CaseData caseData) {
        return SDONotifyData.builder()
            .respondentLastName(getFirstRespondentLastName(caseData.getRespondents1()))
            .callout(buildCallout(caseData))
            .caseUrl(getCaseUrl(caseData.getId(), ORDERS))
            .build();
    }
}
