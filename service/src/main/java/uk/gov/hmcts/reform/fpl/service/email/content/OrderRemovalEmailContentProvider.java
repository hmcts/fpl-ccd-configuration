package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.orderremoval.OrderRemovalTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OrderRemovalEmailContentProvider extends AbstractEmailContentProvider {
    private final EmailNotificationHelper helper;

    public OrderRemovalTemplate buildNotificationForOrderRemoval(CaseData caseData, String removalReason) {
        return OrderRemovalTemplate.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseUrl(getCaseUrl(caseData.getId()))
            .lastName(helper.getEldestChildLastName(caseData.getAllChildren()))
            .removalReason(removalReason)
            .build();
    }
}
