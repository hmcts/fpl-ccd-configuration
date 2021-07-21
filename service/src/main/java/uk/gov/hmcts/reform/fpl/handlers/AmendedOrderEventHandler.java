package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.order.AmendedOrderEvent;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AmendedOrderEventHandler {

    private final ModifiedDocumentCommonEventHandler modifiedDocumentCommonEventHandler;

    @Async
    @EventListener
    @SuppressWarnings("unchecked")
    public void notifyDigitalRepresentatives(final AmendedOrderEvent orderEvent) {
        modifiedDocumentCommonEventHandler.notifyDigitalRepresentatives(orderEvent);
    }

    @Async
    @EventListener
    @SuppressWarnings("unchecked")
    public void notifyEmailRepresentatives(final AmendedOrderEvent orderEvent) {
        modifiedDocumentCommonEventHandler.notifyEmailRepresentatives(orderEvent);
    }

    @Async
    @EventListener
    @SuppressWarnings("unchecked")
    public void notifyLocalAuthority(final AmendedOrderEvent orderEvent) {
        modifiedDocumentCommonEventHandler.notifyLocalAuthority(orderEvent);
    }

    @Async
    @EventListener
    public void sendOrderByPost(final AmendedOrderEvent orderEvent) {
        modifiedDocumentCommonEventHandler.sendOrderByPost(orderEvent);
    }
}
