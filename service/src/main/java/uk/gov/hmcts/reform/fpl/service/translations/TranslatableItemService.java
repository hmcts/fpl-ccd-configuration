package uk.gov.hmcts.reform.fpl.service.translations;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.event.UploadTranslationsEventData;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TranslatableItemService {

    private final TranslatableListItemProviders providers;
    private static final String MEDIA_TYPE = RenderFormat.PDF.getMediaType();

    private final UploadDocumentService uploadService;
    private final TranslatedDocumentGenerator translatedDocumentGenerator;
    private final TranslatedFileNameGenerator translatedFileNameGenerator;

    public DocumentReference getSelectedOrder(CaseData caseData) {
        UUID selectedOrderId = caseData.getUploadTranslationsEventData()
            .getUploadTranslationsRelatedToDocument()
            .getValueCodeAsUUID();

        UploadTranslationsEventData eventData = caseData.getUploadTranslationsEventData();

        TranslatableListItemProvider translatableProvider = providers.getAll().stream()
            .filter(provider -> provider.accept(caseData, selectedOrderId))
            .findFirst()
            .orElseThrow(() -> new UnsupportedOperationException(String.format(
                "Could not find action to amend order for order with id \"%s\"",
                eventData.getUploadTranslationsRelatedToDocument().getValueCode()
            )));

        return translatableProvider.provideSelectedItemDocument(caseData,selectedOrderId);
    }


    public Map<String, Object> finalise(CaseData caseData) {
        UploadTranslationsEventData eventData = caseData.getUploadTranslationsEventData();

        UUID selectedOrderId = caseData.getUploadTranslationsEventData()
            .getUploadTranslationsRelatedToDocument()
            .getValueCodeAsUUID();

        TranslatableListItemProvider translatableProvider = providers.getAll().stream()
            .filter(provider -> provider.accept(caseData, selectedOrderId))
            .findFirst()
            .orElseThrow(() -> new UnsupportedOperationException(String.format(
                "Could not find action to amend order for order with id \"%s\"",
                eventData.getUploadTranslationsRelatedToDocument().getValueCode()
            )));

        byte[] finalisedTranslatedByes = translatedDocumentGenerator.generate(caseData);

        Document stampedDocument = uploadService.uploadDocument(finalisedTranslatedByes,
            translatedFileNameGenerator.generate(caseData),
            MEDIA_TYPE);

        return translatableProvider.applyTranslatedOrder(
            caseData, DocumentReference.buildFromDocument(stampedDocument), selectedOrderId);
    }

}
