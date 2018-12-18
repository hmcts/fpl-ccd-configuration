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
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.emptyCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ExtendWith(SpringExtension.class)
class DocumentGeneratorServiceTest {

    @Test
    void shouldGenerateSubmittedFormDocumentWhenCaseHasNoData() throws IOException {
        CaseDetails caseDetails = emptyCaseDetails();
        caseDetails.getData().put("userFullName", "Emma Taylor");

        String content = textContentOf(createServiceInstance().generateSubmittedFormPDF(caseDetails));

        assertThat(content).contains("C110A");
    }

    @Test
    void shouldGenerateSubmittedFormDocumentWhenCaseIsFullyPopulated() throws IOException {
        Clock clock = Clock.fixed(Instant.parse("2018-11-26T00:00:00Z"), ZoneId.systemDefault());

        CaseDetails caseDetails = populatedCaseDetails();
        caseDetails.getData().put("userFullName", "Emma Taylor");

        String content = textContentOf(createServiceInstance(clock).generateSubmittedFormPDF(caseDetails));
        String expectedContent = ResourceReader.readString("submitted-form-pdf-content.txt");

        assertThat(splitContentIntoTrimmedLines(content))
            .containsExactlyInAnyOrderElementsOf(splitContentIntoTrimmedLines(expectedContent));
    }

    private List<String> splitContentIntoTrimmedLines(String content) {
        return Stream.of(content.split("\n")).map(String::trim).collect(Collectors.toList());
    }

    @Test
    void shouldThrowExceptionWhenTemplateIsTemplateIsMalformed() {
        assertThatThrownBy(() -> createServiceInstance().generateSubmittedFormPDF(null))
            .isInstanceOf(MalformedTemplateException.class);
    }

    private DocumentGeneratorService createServiceInstance() {
        return createServiceInstance(Clock.systemDefaultZone());
    }

    private DocumentGeneratorService createServiceInstance(Clock clock) {
        return new DocumentGeneratorService(
            new DocumentGeneratorConfiguration().getConverter(clock),
            new DocumentTemplates(),
            new ObjectMapper()
        );
    }

    private static String textContentOf(byte[] bytes) throws IOException {
        try (PDDocument document = PDDocument.load(bytes)) {
            return new PDFTextStripper().getText(document);
        }
    }
}
