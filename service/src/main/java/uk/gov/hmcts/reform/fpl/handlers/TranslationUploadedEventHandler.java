package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.ModifiedOrderType;
import uk.gov.hmcts.reform.fpl.events.TranslationUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReferenceWithLanguage;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.SendDocumentRequest;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TranslationUploadedEventHandler {

    private final ModifiedDocumentCommonEventHandler modifiedDocumentCommonEventHandler;
    private final SendDocumentService sendDocumentService;
    private final OtherRecipientsInbox otherRecipientsInbox;

    @Async
    @EventListener
    public void notifyDigitalRepresentatives(final TranslationUploadedEvent orderEvent) {
        modifiedDocumentCommonEventHandler.notifyDigitalRepresentatives(orderEvent);
    }

    @Async
    @EventListener
    public void notifyEmailRepresentatives(final TranslationUploadedEvent orderEvent) {
        modifiedDocumentCommonEventHandler.notifyEmailRepresentatives(orderEvent);
    }

    @Async
    @EventListener
    public void notifyLocalAuthority(final TranslationUploadedEvent orderEvent) {
        modifiedDocumentCommonEventHandler.notifyLocalAuthority(orderEvent);
    }

    @Async
    @EventListener
    public void sendOrderByPost(final TranslationUploadedEvent orderEvent) {
        final CaseData caseData = orderEvent.getCaseData();
        final String orderType = orderEvent.getAmendedOrderType();
        final List<Element<Other>> selectedOthers = orderEvent.getSelectedOthers();

        if (!ModifiedOrderType.STANDARD_DIRECTION_ORDER.getLabel().equals(orderType)) {
            Set<Recipient> allRecipients = new LinkedHashSet<>(sendDocumentService.getStandardRecipients(caseData));

            allRecipients.removeAll(otherRecipientsInbox.getNonSelectedRecipients(
                POST, caseData, selectedOthers, Element::getValue
            ));
            allRecipients.addAll(otherRecipientsInbox.getSelectedRecipientsWithNoRepresentation(selectedOthers));

            sendDocumentService.sendDocuments(
                new SendDocumentRequest(caseData,
                    List.of(
                        DocumentReferenceWithLanguage.builder()
                            .documentReference(orderEvent.getOriginalDocument())
                            .language(orderEvent.getTranslationRequirements().getSourceLanguage().get())
                            .build(),
                        DocumentReferenceWithLanguage.builder()
                            .documentReference(orderEvent.getAmendedDocument())
                            .language(orderEvent.getTranslationRequirements().getTargetLanguage().get())
                            .build()
                    ), new ArrayList<>(allRecipients)));


        }
    }
}
