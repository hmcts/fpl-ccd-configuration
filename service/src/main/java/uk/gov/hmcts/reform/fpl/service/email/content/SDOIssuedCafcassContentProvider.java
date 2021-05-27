package uk.gov.hmcts.reform.fpl.service.email.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.SDONotifyData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCallout;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Component
public class SDOIssuedCafcassContentProvider extends AbstractEmailContentProvider {
    public SDONotifyData getNotifyData(CaseData caseData, DocumentReference order) {
        return SDONotifyData.builder()
            .documentLink(linkToAttachedDocument(order))
            .lastName(getFirstRespondentLastName(caseData.getRespondents1()))
            .callout(buildCallout(caseData))
            .build();
    }
}
