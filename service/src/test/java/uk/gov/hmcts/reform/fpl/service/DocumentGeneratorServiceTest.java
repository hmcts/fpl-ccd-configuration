package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.templates.DocumentTemplates;
import uk.gov.hmcts.reform.pdf.generator.exception.MalformedTemplateException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.emptyCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ExtendWith(SpringExtension.class)
public class DocumentGeneratorServiceTest {

    private DocumentGeneratorService documentGeneratorService = new DocumentGeneratorService(
        new DocumentTemplates(), new ObjectMapper()
    );

    @Test
    public void testEmptyCaseDetailsSuccessfullyReturnsByteArray() throws IOException {
        CaseDetails caseDetails = emptyCaseDetails();

        String content = textContentOf(documentGeneratorService.generateSubmittedFormPDF(caseDetails));

        assertThat(content).contains("Case ID: 123");
    }

    @Test
    public void testPopulatedCaseDetailsSuccessfullyReturnsByteArray() throws IOException {
        CaseDetails caseDetails = populatedCaseDetails();

        String content = textContentOf(documentGeneratorService.generateSubmittedFormPDF(caseDetails));

        assertThat(content).contains("Case ID: 12345");
    }

    @Test
    public void testNullCaseDetailsProvidesMalformedTemplate() {
        Assertions.assertThrows(MalformedTemplateException.class, () -> {
            documentGeneratorService.generateSubmittedFormPDF(null);
        });
    }

    private static String textContentOf(byte[] bytes) throws IOException {
        try (PDDocument document = PDDocument.load(bytes)) {
            return new PDFTextStripper().getText(document);
        }
    }
}
