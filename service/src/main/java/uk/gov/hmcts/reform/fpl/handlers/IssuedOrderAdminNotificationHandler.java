package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.IssuedOrderType;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class IssuedOrderAdminNotificationHandler {
    private final NotificationService notificationService;
    private final HmctsEmailContentProvider hmctsEmailContentProvider;
    private final OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;

    public void sendOrderIssuedNotificationToAdmin(final EventData eventData,
                                                    final byte[] documentContents,
                                                    final IssuedOrderType issuedOrderType) {
        Map<String, Object> parameters = orderIssuedEmailContentProvider.buildNotificationParametersForHmctsAdmin(
            eventData.getCaseDetails(), eventData.getLocalAuthorityCode(), documentContents, issuedOrderType);

        String email = hmctsEmailContentProvider.getHmctsAdminEmail(eventData);

        notificationService.sendEmail(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN, email, parameters,
            Long.toString(eventData.getCaseDetails().getId()));
    }
}
