package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.IssuedOrderType;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RepresentativeNotificationHandler {
    private final OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;
    private final RepresentativeNotificationService representativeNotificationService;

    public void sendOrderIssuedNotificationToRepresentatives(final EventData eventData,
                                                             final byte[] documentContents,
                                                             final List<Representative> representatives,
                                                             final IssuedOrderType issuedOrderType) {
        if (!representatives.isEmpty()) {
            Map<String, Object> parameters =
                orderIssuedEmailContentProvider.buildNotificationParametersForRepresentatives(
                    eventData.getCaseDetails(), eventData.getLocalAuthorityCode(), documentContents, issuedOrderType);

            representativeNotificationService.sendNotificationToRepresentatives(eventData, parameters, representatives,
                ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES);
        } else {
            log.debug("No notification sent to representatives (none require serving)");
        }
    }
}
