package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.TranslationUploadedEvent;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TranslationUploadedEventHandler {

    private final ModifiedDocumentCommonEventHandler modifiedDocumentCommonEventHandler;

    @Async
    @EventListener
    @SuppressWarnings("unchecked")
    public void notifyDigitalRepresentatives(final TranslationUploadedEvent orderEvent) {
        modifiedDocumentCommonEventHandler.notifyDigitalRepresentatives(orderEvent);
    }

    @Async
    @EventListener
    @SuppressWarnings("unchecked")
    public void notifyEmailRepresentatives(final TranslationUploadedEvent orderEvent) {
        modifiedDocumentCommonEventHandler.notifyEmailRepresentatives(orderEvent);
    }

    @Async
    @EventListener
    @SuppressWarnings("unchecked")
    public void notifyLocalAuthority(final TranslationUploadedEvent orderEvent) {
        modifiedDocumentCommonEventHandler.notifyLocalAuthority(orderEvent);
    }

    @Async
    @EventListener
    public void sendOrderByPost(final TranslationUploadedEvent orderEvent) {
        modifiedDocumentCommonEventHandler.sendOrderByPost(orderEvent);
    }

}
