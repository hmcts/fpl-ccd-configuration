package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.IssuedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class IssuedOrderAdminNotificationHandler {

    private final CourtService courtService;
    private final NotificationService notificationService;
    private final OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;

    public void notifyAdmin(final CaseData caseData,
                            final DocumentReference document,
                            final IssuedOrderType issuedOrderType) {
        NotifyData notifyData = orderIssuedEmailContentProvider
            .getNotifyDataWithCaseUrl(caseData, document, issuedOrderType);

        String recipient = courtService.getCourtEmail(caseData);

        notificationService
            .sendEmail(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN, recipient, notifyData, caseData.getId());
    }
}
