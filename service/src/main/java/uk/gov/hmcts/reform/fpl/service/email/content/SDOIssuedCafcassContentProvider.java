package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.SDONotifyData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCallout;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SDOIssuedCafcassContentProvider extends AbstractEmailContentProvider {

    private final EmailNotificationHelper helper;

    public SDONotifyData getNotifyData(CaseData caseData, DocumentReference order) {
        return SDONotifyData.builder()
            .documentLink(linkToAttachedDocument(order))
            .lastName(helper.getSubjectLineLastName(caseData))
            .callout(buildCallout(caseData))
            .build();
    }
}
