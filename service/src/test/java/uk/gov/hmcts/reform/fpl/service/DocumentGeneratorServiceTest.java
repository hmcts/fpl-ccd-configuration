package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.DocumentGeneratorConfiguration;
import uk.gov.hmcts.reform.fpl.templates.DocumentTemplates;
import uk.gov.hmcts.reform.fpl.utils.ResourceReader;
import uk.gov.hmcts.reform.pdf.generator.exception.MalformedTemplateException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.emptyCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ExtendWith(SpringExtension.class)
class DocumentGeneratorServiceTest {

    private DocumentGeneratorService documentGeneratorService = new DocumentGeneratorService(
        new DocumentGeneratorConfiguration().getConverter(),
        new DocumentTemplates(),
        new ObjectMapper()
    );

    @Test
    void shouldGenerateSubmittedFormDocumentWhenCaseHasNoData() throws IOException {
        CaseDetails caseDetails = emptyCaseDetails();
        String content = textContentOf(documentGeneratorService.generateSubmittedFormPDF(caseDetails));

        assertThat(content).contains("C110A");
    }

    @Test
    void shouldGenerateSubmittedFormDocumentWhenCaseIsFullyPopulated() throws IOException {
        CaseDetails caseDetails = populatedCaseDetails();

        String content = textContentOf(documentGeneratorService.generateSubmittedFormPDF(caseDetails));
        String expectedContent = ResourceReader.readString("submitted-form-pdf-content.txt");

        assertThat(splitContentIntoTrimmedLines(content))
            .containsExactlyInAnyOrderElementsOf(splitContentIntoTrimmedLines(expectedContent));
    }

    private List<String> splitContentIntoTrimmedLines(String content) {
        return Arrays.stream(content.split("\n")).map(String::trim).collect(Collectors.toList());
    }

    @Test
    void shouldThrowExceptionWhenTemplateIsTemplateIsMalformed() {
        assertThatThrownBy(() -> documentGeneratorService.generateSubmittedFormPDF(null))
            .isInstanceOf(MalformedTemplateException.class);
    }

    private static String textContentOf(byte[] bytes) throws IOException {
        try (PDDocument document = PDDocument.load(bytes)) {
            return new PDFTextStripper().getText(document);
        }
    }
}
