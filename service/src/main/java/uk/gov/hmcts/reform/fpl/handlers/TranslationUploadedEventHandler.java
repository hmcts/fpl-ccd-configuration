package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.TranslationUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;
import uk.gov.hmcts.reform.fpl.service.translations.TranslatableItemService;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TranslationUploadedEventHandler {

    private final TranslatableItemService translatableItemService;

    @EventListener
    public void notifyParties(TranslationUploadedEvent event) {

        Element<? extends TranslatableItem> lastTranslatedItem =
            translatableItemService.getLastTranslatedItem(event.getCaseData());

        translatableItemService.notifyToParties(event.getCaseData(), lastTranslatedItem);

    }

}
