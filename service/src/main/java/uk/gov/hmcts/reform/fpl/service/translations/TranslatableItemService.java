package uk.gov.hmcts.reform.fpl.service.translations;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.UploadTranslationsEventData;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;
import uk.gov.hmcts.reform.fpl.service.DynamicListService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.translations.provider.TranslatableListItemProvider;
import uk.gov.hmcts.reform.fpl.service.translations.provider.TranslatableListItemProviders;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static java.util.Comparator.reverseOrder;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TranslatableItemService {

    private static final String MEDIA_TYPE = RenderFormat.PDF.getMediaType();

    private final TranslatableListItemProviders providers;
    private final UploadDocumentService uploadService;
    private final TranslatedDocumentGenerator translatedDocumentGenerator;
    private final TranslatedFileNameGenerator translatedFileNameGenerator;
    private final DynamicListService listService;

    public DynamicList generateList(CaseData caseData) {

        List<Element<? extends TranslatableItem>> translatableOrders = providers.getAll().stream()
            .map(provider -> provider.provideListItems(caseData))
            .flatMap(Collection::stream)
            .filter(item -> item.getValue().translationRequirements().isNeedAction())
            .filter(item -> !item.getValue().hasBeenTranslated())
            .collect(Collectors.toList());

        return listService.asDynamicList(
            translatableOrders,
            order -> order.getId().toString(),
            order -> order.getValue().asLabel()
        );
    }

    public DocumentReference getSelectedOrder(CaseData caseData) {
        UUID selectedOrderId = caseData.getUploadTranslationsEventData()
            .getUploadTranslationsRelatedToDocument()
            .getValueCodeAsUUID();

        UploadTranslationsEventData eventData = caseData.getUploadTranslationsEventData();

        TranslatableListItemProvider translatableProvider = providers.getAll().stream()
            .filter(provider -> provider.accept(caseData, selectedOrderId))
            .findFirst()
            .orElseThrow(() -> new UnsupportedOperationException(String.format(
                "Could not find action to translate item with id \"%s\"",
                eventData.getUploadTranslationsRelatedToDocument().getValueCode()
            )));

        return translatableProvider.provideSelectedItemDocument(caseData, selectedOrderId);
    }


    public Map<String, Object> finalise(CaseData caseData) {
        UploadTranslationsEventData eventData = caseData.getUploadTranslationsEventData();

        UUID selectedOrderId = eventData
            .getUploadTranslationsRelatedToDocument()
            .getValueCodeAsUUID();

        TranslatableListItemProvider translatableProvider = providers.getAll().stream()
            .filter(provider -> provider.accept(caseData, selectedOrderId))
            .findFirst()
            .orElseThrow(() -> new UnsupportedOperationException(String.format(
                "Could not find action to translate item with id \"%s\"",
                eventData.getUploadTranslationsRelatedToDocument().getValueCode()
            )));

        byte[] finalisedTranslatedByes = translatedDocumentGenerator.generate(caseData);

        Document stampedDocument = uploadService.uploadDocument(finalisedTranslatedByes,
            translatedFileNameGenerator.generate(caseData),
            MEDIA_TYPE);

        return translatableProvider.applyTranslatedOrder(
            caseData, DocumentReference.buildFromDocument(stampedDocument), selectedOrderId);
    }

    public Element<? extends TranslatableItem> getLastTranslatedItem(CaseData caseData) {
        return providers.getAll().stream()
            .flatMap(provider -> provider.provideListItems(caseData).stream())
            .filter(item -> item.getValue().hasBeenTranslated())
            .min(comparing(order -> order.getValue().translationUploadDateTime(), nullsLast(reverseOrder())))
            .orElseThrow(() -> new UnsupportedOperationException("Could not find a translated item"));
    }

}
