package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.OrderAmendedNotifyData;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.ORDERS;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCallout;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AmendedOrderEmailContentProvider extends AbstractEmailContentProvider {
    private final CourtService courtService;
    private final EmailNotificationHelper helper;

    public OrderAmendedNotifyData getNotifyData(final CaseData caseData,
                                                final DocumentReference orderDocument,
                                                final String orderType) {

        return OrderAmendedNotifyData.builder()
            .lastName(helper.getEldestChildLastName(caseData.getAllChildren()))
            .orderType(orderType)
            .courtName(courtService.getCourtName(caseData))
            .callout("^" + buildCallout(caseData))
            .documentLink(getDocumentUrl(orderDocument))
            .caseUrl(getCaseUrl(caseData.getId(), ORDERS))
            .build();
    }

}
