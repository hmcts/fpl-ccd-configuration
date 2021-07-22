package uk.gov.hmcts.reform.fpl.service.translation;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisTranslationRequest;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.TRANSLATION_REQUEST;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TranslationRequestFormCreationService {
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    public DocmosisDocument buildTranslationRequestDocuments(DocmosisTranslationRequest templateData) {
        return docmosisDocumentGeneratorService.generateDocmosisDocument(
            templateData,
            TRANSLATION_REQUEST,
            templateData.getFormat()
        );
    }
}
