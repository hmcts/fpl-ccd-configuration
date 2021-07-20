package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.events.TranslationUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;
import uk.gov.hmcts.reform.fpl.service.translations.TranslatableItemService;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class TranslationUploadedEventHandlerTest {

    private static final CaseData CASE_DATA = mock(CaseData.class);
    private static final Element<TranslatableItem> ELEMENT = element(mock(TranslatableItem.class));
    private final TranslatableItemService translatableItemService = mock(
        TranslatableItemService.class);

    private final TranslationUploadedEventHandler underTest =
        new TranslationUploadedEventHandler(translatableItemService);

    @Test
    void notifyParties() {
        doReturn(ELEMENT).when(translatableItemService).getLastTranslatedItem(CASE_DATA);

        underTest.notifyParties(new TranslationUploadedEvent(CASE_DATA));

        verify(translatableItemService).notifyToParties(CASE_DATA,ELEMENT);
    }
}
