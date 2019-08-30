package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.fpl.config.DocumentGeneratorConfiguration;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.templates.DocumentTemplates;
import uk.gov.hmcts.reform.fpl.utils.ResourceReader;
import uk.gov.hmcts.reform.pdf.generator.exception.MalformedTemplateException;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.emptyCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.migratedChildCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ExtendWith(SpringExtension.class)
class DocumentGeneratorServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ResponseEntity<byte[]> tornadoResponse;

    private String tornadoUrl = "http://tornado:5433";

    @Test
    void shouldGenerateSubmittedFormDocumentWhenCaseHasNoData() throws IOException {
        Clock clock = Clock.fixed(Instant.parse("2019-08-02T00:00:00Z"), ZoneId.systemDefault());

        String content = textContentOf(createServiceInstance(clock).generateSubmittedFormPDF(emptyCaseDetails(),
            Pair.of("userFullName", "Emma Taylor"))
        );

        String expectedContent = ResourceReader.readString("empty-form-pdf-content.txt");

        assertThat(splitContentIntoTrimmedLines(content))
            .containsExactlyInAnyOrderElementsOf(splitContentIntoTrimmedLines(expectedContent));
    }

    @Test
    void shouldGenerateSubmittedFormDocumentWhenCaseIsFullyPopulated() throws IOException {
        Clock clock = Clock.fixed(Instant.parse("2018-11-26T00:00:00Z"), ZoneId.systemDefault());

        String content = textContentOf(createServiceInstance(clock).generateSubmittedFormPDF(populatedCaseDetails(),
            Pair.of("userFullName", "Emma Taylor"))
        );

        String expectedContent = ResourceReader.readString("submitted-form-pdf-content.txt");

        assertThat(splitContentIntoTrimmedLines(content))
            .containsExactlyInAnyOrderElementsOf(splitContentIntoTrimmedLines(expectedContent));
    }

    @Test
    void shouldGenerateSubmittedFormWhenCaseHasBothOldAndNewChildStructure() throws IOException {
        Clock clock = Clock.fixed(Instant.parse("2018-11-26T00:00:00Z"), ZoneId.systemDefault());

        String content = textContentOf(
            createServiceInstance(clock).generateSubmittedFormPDF(migratedChildCaseDetails(),
                Pair.of("userFullName", "Emma Taylor"))
        );
        String expectedContent = ResourceReader.readString("submitted-form-pdf-content.txt");

        assertThat(splitContentIntoTrimmedLines(content))
            .containsExactlyInAnyOrderElementsOf(splitContentIntoTrimmedLines(expectedContent));
    }

    private List<String> splitContentIntoTrimmedLines(String content) {
        return Stream.of(content.split("\n")).map(String::trim).collect(Collectors.toList());
    }

    @Test
    void shouldThrowExceptionWhenTemplateIsMalformed() {
        assertThatThrownBy(() -> createServiceInstance().generateSubmittedFormPDF(null))
            .isInstanceOf(MalformedTemplateException.class);
    }

    @Test
    void shouldInvokesTornado() {
        Map<String, String> placeholders = Map.of("applicant", "John Smith");

        when(restTemplate.exchange(eq(tornadoUrl), eq(HttpMethod.POST), any(), eq(byte[].class)))
            .thenReturn(tornadoResponse);

        byte[] expectedResponse = {1, 2 ,3};
        when(tornadoResponse.getBody()).thenReturn(expectedResponse);


        DocmosisDocument docmosisDocument = createServiceInstance().generateDocmosisDocument(placeholders, C6);
        assertThat(docmosisDocument.getBytes()).isEqualTo(expectedResponse);
    }

    private DocumentGeneratorService createServiceInstance() {
        return createServiceInstance(Clock.systemDefaultZone());
    }

    private DocumentGeneratorService createServiceInstance(Clock clock) {
        return new DocumentGeneratorService(
            new DocumentGeneratorConfiguration().getConverter(clock),
            new DocumentTemplates(),
            new ObjectMapper(),
            restTemplate,
            tornadoUrl
        );
    }

    private static String textContentOf(byte[] bytes) throws IOException {
        try (PDDocument document = PDDocument.load(bytes)) {
            return new PDFTextStripper().getText(document);
        }
    }
}
