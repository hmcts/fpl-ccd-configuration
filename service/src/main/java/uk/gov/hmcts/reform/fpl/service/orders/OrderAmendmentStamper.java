package uk.gov.hmcts.reform.fpl.service.orders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.util.Matrix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.ResourceReader;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;

import static org.apache.commons.io.FilenameUtils.getExtension;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_SHORT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.hasExtension;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OrderAmendmentStamper {

    private static final String PDF = "pdf";
    private static final String MEDIA_TYPE = RenderFormat.PDF.getMediaType();
    private static final float FONT_SIZE = 16f;
    private static final String FONT_LOCATION = "fonts/arial_bold.ttf";

    private final UploadDocumentService uploadService;
    private final DocumentDownloadService downloadService;
    private final Time time;

    public DocumentReference amendDocument(DocumentReference original) {
        if (!hasExtension(original, PDF)) {
            throw new UnsupportedOperationException(
                "Can only amend documents that are pdf, requested document was of type: "
                + getExtension(original.getFilename())
            );
        }

        byte[] documentContents = downloadService.downloadDocument(original.getBinaryUrl());

        try {
            documentContents = amendDocument(documentContents);
        } catch (IOException e) {
            log.error("Could not add amendment text to {}", original, e);
            throw new UncheckedIOException(e);
        }

        return buildFromDocument(uploadService.uploadDocument(documentContents, updateFileName(original), MEDIA_TYPE));
    }

    private byte[] amendDocument(byte[] binaries) throws IOException {
        try (PDDocument document = PDDocument.load(binaries)) {
            final ByteArrayInputStream font_binaries = new ByteArrayInputStream(ResourceReader.readBytes(FONT_LOCATION));
            final PDFont font = PDType0Font.load(document, font_binaries);

            final PDPage page = document.getPage(0);

            // build message
            final LocalDate now = time.now().toLocalDate();
            final String message = "Amended under the slip rule - " + formatLocalDateToString(now, DATE_SHORT);

            // message properties
            final PDRectangle pageSize = page.getMediaBox();
            final float messageWidth = font.getStringWidth(message) * FONT_SIZE / 1000f;
            final float messageHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() * FONT_SIZE / 1000f;
            final float x = (pageSize.getWidth() - messageWidth) / 2f; // centred
            final float y = pageSize.getHeight() - messageHeight * 2f; // second line
            final Matrix messageLocation = Matrix.getTranslateInstance(x, y);

            final PDPageContentStream content = new PDPageContentStream(document, page, AppendMode.APPEND, true, true);

            // hide previous amendment message
            content.setNonStrokingColor(Color.WHITE);
            content.addRect(x, y, messageWidth, messageHeight);
            content.fill();

            // write new amendment message
            content.beginText();
            content.setNonStrokingColor(Color.RED);
            content.setFont(font, FONT_SIZE);
            content.setTextMatrix(messageLocation);
            content.showText(message);
            content.endText();

            content.close();

            return save(document);
        }
    }

    private static byte[] save(PDDocument document) throws IOException {
        try (final ByteArrayOutputStream outputBytes = new ByteArrayOutputStream()) {
            document.save(outputBytes);
            return outputBytes.toByteArray();
        }
    }

    private String updateFileName(DocumentReference original) {
        return "amended_" + original.getFilename();
    }
}
