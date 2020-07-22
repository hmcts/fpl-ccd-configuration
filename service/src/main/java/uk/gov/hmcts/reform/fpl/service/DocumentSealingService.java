package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.utils.ResourceReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode.APPEND;
import static org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject.createFromByteArray;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentSealingService {
    private static final float POINTS_PER_INCH = 72;
    private static final float POINTS_PER_MM = 1 / (10 * 2.54f) * POINTS_PER_INCH;
    private static final int SEAL_HEIGHT = mm2pt(25);
    private static final int SEAL_WIDTH = mm2pt(25);
    private static final int MARGIN_TOP = mm2pt(30);
    private static final int MARGIN_RIGHT = mm2pt(30);

    private final DocumentDownloadService documentDownloadService;
    private final UploadDocumentService uploadDocumentService;

    public DocumentReference sealDocument(DocumentReference document) throws Exception {
        return sealDocument(document, null);
    }

    public DocumentReference sealDocument(DocumentReference document, String password) throws Exception {
        byte[] documentContent = documentDownloadService.downloadDocument(document.getBinaryUrl());
        byte[] sealedDocument = stampDocument(documentContent, password);

        return DocumentReference.buildFromDocument(
            uploadDocumentService.uploadPDF(sealedDocument, document.getFilename()));
    }

    private byte[] stampDocument(byte[] inputDocInBytes, String password) throws Exception {
        byte[] image = getSealImage();

        try (PDDocument doc = loadDocument(inputDocInBytes, password)) {
            final PDPage page = doc.getPage(0);
            final PDRectangle pageSize = page.getTrimBox();
            try (PDPageContentStream psdStream = new PDPageContentStream(doc, page, APPEND, true, true)) {
                final PDImageXObject courtSealImage = createFromByteArray(doc, image, null);
                psdStream.drawImage(courtSealImage,
                    pageSize.getUpperRightX() - (SEAL_WIDTH + MARGIN_RIGHT),
                    pageSize.getUpperRightY() - (SEAL_HEIGHT + MARGIN_TOP),
                    SEAL_WIDTH,
                    SEAL_HEIGHT);
            }

            return saveDocument(doc, password);
        }
    }

    private static PDDocument loadDocument(byte[] content, String password) throws IOException {
        return ObjectUtils.isEmpty(password) ? PDDocument.load(content) : PDDocument.load(content, password);

    }

    private static byte[] getSealImage() {
        return ResourceReader.readBytes("static_data/familycourtseal.png");
    }

    private static int mm2pt(int mm) {
        return Math.round(POINTS_PER_MM * mm);
    }

    private byte[] saveDocument(PDDocument document, String password) throws IOException {
        try (ByteArrayOutputStream outputBytes = new ByteArrayOutputStream()) {
            document.setAllSecurityToBeRemoved(true);
            //document.protect(new StandardProtectionPolicy(password, password, document.getCurrentAccessPermission()
            // ));
            document.save(outputBytes);
            return outputBytes.toByteArray();
        }
    }

}
