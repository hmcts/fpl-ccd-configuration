package uk.gov.hmcts.reform.fpl.service.email.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.SDONotifyData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCallout;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Component
public class SDOIssuedCafcassContentProvider extends AbstractEmailContentProvider {
    public SDONotifyData getNotifyData(CaseData caseData) {
        return SDONotifyData.builder()
            .documentLink(linkToAttachedDocument(caseData.getStandardDirectionOrder().getOrderDoc()))
            .leadRespondentsName(getFirstRespondentLastName(caseData.getAllRespondents()))
            .callout(buildCallout(caseData))
            .build();
    }
}
