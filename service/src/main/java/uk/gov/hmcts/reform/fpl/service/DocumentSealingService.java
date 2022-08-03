package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.exceptions.EncryptedPdfUploadedException;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import javax.imageio.ImageIO;

import static java.time.LocalDate.now;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode.APPEND;
import static org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject.createFromByteArray;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.HIGH_COURT_SEAL;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.updateExtension;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class DocumentSealingService {

    private static final String SEAL = "static_data/familycourtseal.png";
    private static final String PDF = "pdf";
    private static final float POINTS_PER_INCH = 72;
    private static final float POINTS_PER_MM = 1 / (10 * 2.54f) * POINTS_PER_INCH;
    private static final int SEAL_HEIGHT = mm2pt(25);
    private static final int SEAL_WIDTH = mm2pt(25);
    private static final int MARGIN_TOP = mm2pt(30);
    private static final int MARGIN_RIGHT = mm2pt(30);

    private final UploadDocumentService uploadDocumentService;
    private final DocumentConversionService documentConversionService;
    private final DocumentDownloadService documentDownloadService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final CourtService courtService;

    public DocumentReference sealDocument(DocumentReference applicationDocument,
                                          Court court,
                                          SealType sealType) {
        byte[] seal = getSeal(court, sealType);
        return sealDocument(applicationDocument, seal);
    }

    public byte[] sealDocument(byte[] binaries,
                               Court court,
                               SealType sealType) {
        byte[] seal = getSeal(court, sealType);
        return sealDocument(binaries, seal);
    }

    private DocumentReference sealDocument(DocumentReference document, byte[] seal) {
        byte[] documentContents = documentDownloadService.downloadDocument(document.getBinaryUrl());
        documentContents = documentConversionService.convertToPdf(documentContents, document.getFilename());
        documentContents = sealDocument(documentContents, seal);

        String newFilename = updateExtension(document.getFilename(), PDF);

        return buildFromDocument(uploadDocumentService.uploadPDF(documentContents, newFilename));
    }

    private  byte[] sealDocument(byte[] binaries, byte[] seal) {
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
        } catch (IllegalStateException ise) {
            if (defaultIfNull(ise.getMessage(), "").startsWith("PDF contains an encryption dictionary")) {
                throw new EncryptedPdfUploadedException("Encrypted PDF was uploaded.");
            } else {
                throw ise;
            }
        } catch (InvalidPasswordException ipe) {
            throw new EncryptedPdfUploadedException("Password protected PDF was uploaded.");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private byte[] getSeal(Court court, SealType sealType) {
        byte[] seal;
        if (courtService.isHighCourtCase(court)) {
            DocmosisDocument docmosisDocument = docmosisDocumentGeneratorService.generateDocmosisDocument(
                    Map.of("dateOfIssue", formatLocalDateToString(now(), DATE)),
                            HIGH_COURT_SEAL);
            byte[] bytes = documentConversionService.convertToPdf(
                    docmosisDocument.getBytes(),
                    docmosisDocument.getDocumentTitle()
            );
            try (PDDocument document = PDDocument.load(bytes)) {
                PDFRenderer renderer = new PDFRenderer(document);
                BufferedImage image = renderer.renderImage(0);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "PNG", baos);
                seal = baos.toByteArray();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                seal = readBytes(SealType.HIGHCOURT_ENGLISH.getImage());
            }
        } else {
            seal = readBytes(sealType.getImage());
        }
        return seal;
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
