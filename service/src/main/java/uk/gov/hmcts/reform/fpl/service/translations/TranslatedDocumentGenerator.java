package uk.gov.hmcts.reform.fpl.service.translations;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class TranslatedDocumentGenerator {

    private final DocumentSealingService documentSealingService;
    private final DocumentConversionService documentConversionService;
    private final DocumentDownloadService documentDownloadService;

    public byte[] generate(CaseData caseData) {
        DocumentReference documentReference = caseData.getUploadTranslationsEventData()
            .getUploadTranslationsTranslatedDoc();
        byte[] bytes = documentDownloadService.downloadDocument(documentReference.getBinaryUrl());

        byte[] convertedPdfBytes = documentConversionService.convertToPdf(bytes, documentReference.getFilename());
        return documentSealingService.sealDocument(convertedPdfBytes, caseData.getCourt(), SealType.BILINGUAL);
    }

}
