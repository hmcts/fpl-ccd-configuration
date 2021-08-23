package uk.gov.hmcts.reform.fpl.docmosis;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.springframework.stereotype.Component;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.UncheckedIOException;

@Component
public class DocmosisHelper {

    private static final float POINTS_PER_INCH = 72;
    private static final float POINTS_PER_MM = 1 / (10 * 2.54f) * POINTS_PER_INCH;
    private static final int FOOTER_HEIGHT_IN_MM = 15;
    private static final int FOOTER_HEIGHT_IN_POINTS = Math.round(POINTS_PER_MM * FOOTER_HEIGHT_IN_MM);
    private static final String DATA_REGION = "dataRegion";

    public String extractPdfContent(byte[] binaries) {

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

    public String remove(String text, String... ignores) {
        String updatedText = text;
        for (String ignore : ignores) {
            updatedText = updatedText.replaceAll(ignore, "");
        }
        return updatedText;
    }
}
