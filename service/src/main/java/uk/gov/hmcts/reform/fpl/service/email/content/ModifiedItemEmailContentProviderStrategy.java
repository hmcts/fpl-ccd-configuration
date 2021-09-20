package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.ModifiedDocumentEvent;
import uk.gov.hmcts.reform.fpl.events.TranslationUploadedEvent;
import uk.gov.hmcts.reform.fpl.events.order.AmendedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.notify.ModifiedItemEmailContentProviderResponse;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ITEM_TRANSLATED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_AMENDED_NOTIFICATION_TEMPLATE;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ModifiedItemEmailContentProviderStrategy {

    private final AmendedOrderEmailContentProvider amendedOrderEmailContentProvider;
    private final TranslatedItemEmailContentProvider translatedItemEmailContentProvider;

    public ModifiedItemEmailContentProviderResponse getEmailContentProvider(
        ModifiedDocumentEvent orderEvent) {
        if (orderEvent instanceof AmendedOrderEvent) {
            return ModifiedItemEmailContentProviderResponse.builder()
                .provider(amendedOrderEmailContentProvider)
                .templateKey(ORDER_AMENDED_NOTIFICATION_TEMPLATE)
                .build();
        }
        if (orderEvent instanceof TranslationUploadedEvent) {
            return ModifiedItemEmailContentProviderResponse.builder()
                .provider(translatedItemEmailContentProvider)
                .templateKey(ITEM_TRANSLATED_NOTIFICATION_TEMPLATE)
                .build();
        }

        throw new IllegalArgumentException("Provider not found for event of class " + orderEvent.getClass());
    }


}
