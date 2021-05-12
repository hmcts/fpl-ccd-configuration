package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import net.serenitybdd.rest.SerenityRest;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.User;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.UncheckedIOException;

import static org.apache.http.HttpStatus.SC_OK;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentService {

    private static final float POINTS_PER_INCH = 72;
    private static final float POINTS_PER_MM = 1 / (10 * 2.54f) * POINTS_PER_INCH;
    private static final int FOOTER_HEIGHT_IN_MM = 15;
    private static final int FOOTER_HEIGHT_IN_POINTS = Math.round(POINTS_PER_MM * FOOTER_HEIGHT_IN_MM);
    private static final String DATA_REGION = "dataRegion";

    private final AuthenticationService authenticationService;

    //TODO Local env does not have required font, which cause different page layout, thus footer and header removal
    public String getPdfContent(DocumentReference documentReference, User user, String... ignores) {
        byte[] binaries = getDocument(documentReference, user);
        String text = extractPdfContent(binaries);
        return remove(text, ignores);
    }

    private byte[] getDocument(DocumentReference documentReference, User user) {
        return SerenityRest
            .given()
            .headers(authenticationService.getAuthorizationHeaders(user))
            .body(documentReference.getBinaryUrl())
            .get("/testing-support/document")
            .then()
            .statusCode(SC_OK)
            .extract()
            .asByteArray();
    }

    private String extractPdfContent(byte[] binaries) {

        try (final PDDocument pdf = PDDocument.load(binaries)) {

            final StringBuilder textBuilder = new StringBuilder();

            for (int i = 0; i < pdf.getNumberOfPages(); i++) {
                PDPage page = pdf.getPage(i);

                PDRectangle dimensions = page.getCropBox();

                Rectangle2D dataRegion = new Rectangle2D.Double(0, 0,
                    dimensions.getWidth(), dimensions.getHeight() - FOOTER_HEIGHT_IN_POINTS);

                PDFTextStripperByArea textStripper = new PDFTextStripperByArea();

                textStripper.addRegion(DATA_REGION, dataRegion);
                textStripper.extractRegions(page);

                textBuilder.append(textStripper.getTextForRegion(DATA_REGION));
            }

            return textBuilder.toString();

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String remove(String text, String... ignores) {
        String updatedText = text;
        for (String ignore : ignores) {
            updatedText = updatedText.replaceAll(ignore, "");
        }
        return updatedText;
    }

}
