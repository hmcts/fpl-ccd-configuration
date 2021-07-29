package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.events.TranslationUploadedEvent;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TranslationUploadedEventHandlerTest {

    private static final TranslationUploadedEvent EVENT = mock(TranslationUploadedEvent.class);
    private final ModifiedDocumentCommonEventHandler modifiedDocumentCommonEventHandler = mock(
        ModifiedDocumentCommonEventHandler.class);

    private final TranslationUploadedEventHandler underTest =
        new TranslationUploadedEventHandler(modifiedDocumentCommonEventHandler);

    @Test
    void notifyDigitalRepresentatives() {
        underTest.notifyDigitalRepresentatives(EVENT);

        verify(modifiedDocumentCommonEventHandler).notifyDigitalRepresentatives(EVENT);
    }

    @Test
    void notifyEmailRepresentatives() {
        underTest.notifyEmailRepresentatives(EVENT);

        verify(modifiedDocumentCommonEventHandler).notifyEmailRepresentatives(EVENT);
    }

    @Test
    void notifyLocalAuthority() {
        underTest.notifyLocalAuthority(EVENT);

        verify(modifiedDocumentCommonEventHandler).notifyLocalAuthority(EVENT);
    }

    @Test
    void sendOrderByPost() {
        underTest.sendOrderByPost(EVENT);

        verify(modifiedDocumentCommonEventHandler).sendOrderByPost(EVENT);
    }

}
