package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode.APPEND;
import static org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject.createFromByteArray;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentSealingService {

    private static final String SEAL = "static_data/familycourtseal.png";
    private static final float POINTS_PER_INCH = 72;
    private static final float POINTS_PER_MM = 1 / (10 * 2.54f) * POINTS_PER_INCH;
    private static final int SEAL_HEIGHT = mm2pt(25);
    private static final int SEAL_WIDTH = mm2pt(25);
    private static final int MARGIN_TOP = mm2pt(30);
    private static final int MARGIN_RIGHT = mm2pt(30);

    private final DocumentDownloadService documentDownloadService;
    private final UploadDocumentService uploadDocumentService;

    public DocumentReference sealDocument(DocumentReference document) throws Exception {
        byte[] documentContent = documentDownloadService.downloadDocument(document.getBinaryUrl());
        byte[] sealedDocument = sealDocument(documentContent);

        return buildFromDocument(uploadDocumentService.uploadPDF(sealedDocument, document.getFilename()));
    }

    private static byte[] sealDocument(byte[] binaries) throws Exception {
        byte[] seal = readBytes(SEAL);

        try (final PDDocument document = PDDocument.load(binaries)) {
            final PDPage firstPage = document.getPage(0);
            final PDRectangle pageSize = firstPage.getTrimBox();

            try (PDPageContentStream pdfStream = new PDPageContentStream(document, firstPage, APPEND, true, true)) {
                final PDImageXObject courtSealImage = createFromByteArray(document, seal, null);
                pdfStream.drawImage(courtSealImage,
                        pageSize.getUpperRightX() - (SEAL_WIDTH + MARGIN_RIGHT),
                        pageSize.getUpperRightY() - (SEAL_HEIGHT + MARGIN_TOP),
                        SEAL_WIDTH,
                        SEAL_HEIGHT);
            }

            return getBinary(document);
        }
    }

    private static byte[] getBinary(PDDocument document) throws IOException {
        try (final ByteArrayOutputStream outputBytes = new ByteArrayOutputStream()) {
            document.save(outputBytes);
            return outputBytes.toByteArray();
        }
    }

    private static int mm2pt(int mm) {
        return Math.round(POINTS_PER_MM * mm);
    }

}
