package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.events.ModifiedDocumentEvent;
import uk.gov.hmcts.reform.fpl.events.TranslationUploadedEvent;
import uk.gov.hmcts.reform.fpl.events.order.AmendedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.notify.ModifiedItemEmailContentProviderResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ITEM_TRANSLATED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_AMENDED_NOTIFICATION_TEMPLATE;


class ModifiedItemEmailContentProviderStrategyTest {

    private final AmendedOrderEmailContentProvider amendedOrderEmailContentProvider = mock(
        AmendedOrderEmailContentProvider.class);
    private final TranslatedItemEmailContentProvider translatedItemEmailContentProvider = mock(
        TranslatedItemEmailContentProvider.class);

    private final ModifiedItemEmailContentProviderStrategy underTest = new ModifiedItemEmailContentProviderStrategy(
        amendedOrderEmailContentProvider,
        translatedItemEmailContentProvider
    );

    @Test
    void providerForAmendedOrderEvent() {
        ModifiedItemEmailContentProviderResponse actual =
            underTest.getEmailContentProvider(mock(AmendedOrderEvent.class));

        assertThat(actual).isEqualTo(ModifiedItemEmailContentProviderResponse.builder()
            .provider(amendedOrderEmailContentProvider)
            .templateKey(ORDER_AMENDED_NOTIFICATION_TEMPLATE)
            .build());
    }

    @Test
    void providerForTranslationUploadEvent() {
        ModifiedItemEmailContentProviderResponse actual =
            underTest.getEmailContentProvider(mock(TranslationUploadedEvent.class));

        assertThat(actual).isEqualTo(ModifiedItemEmailContentProviderResponse.builder()
            .provider(translatedItemEmailContentProvider)
            .templateKey(ITEM_TRANSLATED_NOTIFICATION_TEMPLATE)
            .build());
    }

    @Test
    void testNoProviderImplemented() {
        ModifiedDocumentEvent orderEvent = mock(ModifiedDocumentEvent.class);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> {
                underTest.getEmailContentProvider(orderEvent);
            });

        assertThat(exception.getMessage()).isEqualTo("Provider not found for event of class " + orderEvent.getClass());
    }
}
