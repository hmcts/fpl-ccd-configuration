package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static io.jsonwebtoken.lang.Collections.isEmpty;
import static org.apache.pdfbox.io.MemoryUsageSetting.setupMainMemoryOnly;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentMerger {

    private final DocumentDownloadService documentDownloadService;
    private final DocumentConversionService documentConversionService;

    public DocmosisDocument mergeDocuments(DocmosisDocument originalDocument,
                                           List<DocumentReference> additionalDocuments) {

        if (isEmpty(additionalDocuments)) {
            return originalDocument;
        }

        ByteArrayOutputStream docOutputStream = new ByteArrayOutputStream();
        List<InputStream> documents = getPdfFilesToMerge(originalDocument, additionalDocuments);

        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        pdfMergerUtility.addSources(documents);
        pdfMergerUtility.setDestinationStream(docOutputStream);
        try {
            pdfMergerUtility.mergeDocuments(setupMainMemoryOnly());
            log.info("Merged {} documents", 1 + additionalDocuments.size());
            return new DocmosisDocument(originalDocument.getDocumentTitle(), docOutputStream.toByteArray());
        } catch (IOException e) {
            throw new DocumentMergeException(
                "Exception occurred while merging documents for " + originalDocument.getDocumentTitle(), e
            );
        }
    }

    private List<InputStream> getPdfFilesToMerge(DocmosisDocument originalDocument,
                                                 List<DocumentReference> additionalDocuments) {
        List<InputStream> documents = new ArrayList<>();
        final byte[] convertedOriginalDocument = documentConversionService.convertToPdf(
            originalDocument.getBytes(), originalDocument.getDocumentTitle());

        documents.add(new ByteArrayInputStream(convertedOriginalDocument));

        additionalDocuments.forEach(documentReference -> {
            final byte[] document = documentDownloadService.downloadDocument(documentReference.getBinaryUrl());
            final byte[] pdf = documentConversionService.convertToPdf(document, documentReference.getFilename());
            documents.add(new ByteArrayInputStream(pdf));
        });
        return documents;
    }

}
