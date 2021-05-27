package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.SDONotifyData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.ORDERS;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCallout;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SDOIssuedContentProvider extends AbstractEmailContentProvider {

    private final EmailNotificationHelper helper;

    public SDONotifyData buildNotificationParameters(CaseData caseData) {
        return SDONotifyData.builder()
            .lastName(helper.getSubjectLineLastName(caseData))
            .callout(buildCallout(caseData))
            .caseUrl(getCaseUrl(caseData.getId(), ORDERS))
            .build();
    }
}
