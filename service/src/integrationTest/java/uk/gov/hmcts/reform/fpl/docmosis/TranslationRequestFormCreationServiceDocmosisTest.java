package uk.gov.hmcts.reform.fpl.docmosis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.docmosis.welshtranslation.DocmosisTranslateLanguages;
import uk.gov.hmcts.reform.fpl.model.docmosis.welshtranslation.DocmosisTranslationRequest;
import uk.gov.hmcts.reform.fpl.model.docmosis.welshtranslation.DocmosisWelshLayout;
import uk.gov.hmcts.reform.fpl.model.docmosis.welshtranslation.DocmosisWelshProject;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.translation.TranslationRequestFormCreationService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.docmosis.DocmosisHelper.extractPdfContent;
import static uk.gov.hmcts.reform.fpl.docmosis.DocmosisHelper.remove;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;

@ContextConfiguration(classes = {
    TranslationRequestFormCreationService.class,
    DocmosisDocumentGeneratorService.class
})

public class TranslationRequestFormCreationServiceDocmosisTest extends AbstractDocmosisTest {

    @Autowired
    private TranslationRequestFormCreationService underTest;

    private static final LocalDate FIXED_DATE = LocalDate.of(2021, 7, 28);

    private DocmosisTranslationRequest request;
    private String expectedContentFileLocation;
    private String generatedContentOutputFile;

    private final byte[] pdf = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    private final DocmosisDocument orderDocumentToTranslate = new DocmosisDocument("example.pdf", pdf);

    public static final String NAME = "Courts and Tribunals Service Centre";
    public static final String DEPARTMENT = "Family Public Law";
    public static final String CONTACT_INFORMATION = "contactfpl@justice.gov.uk";
    public static final String FAMILY_MAN_CASE_NUMBER = "FamilyManCaseNumber123";

    @Test
    void shouldGenerateTranslationFormRequestEnglishToWelsh() throws IOException {
        expectedContentFileLocation = "translation-form-request/EnglishToWelshTranslationDocument.txt";
        generatedContentOutputFile = "EnglishToWelshTranslationDocument.";

        request = buildTranslationRequest(true, orderDocumentToTranslate);

        generateWordDocument();

        assertActualOutputMatchesTestFile();
    }

    @Test
    void shouldGenerateTranslationFormRequestWelshToEnglish() throws IOException {
        expectedContentFileLocation = "translation-form-request/WelshToEnglishTranslationDocument.txt";
        generatedContentOutputFile = "WelshToEnglishTranslationDocument.";

        request = buildTranslationRequest(false, orderDocumentToTranslate);

        generateWordDocument();

        assertActualOutputMatchesTestFile();
    }

    @Test
    void shouldGenerateTranslationFormRequestProjectSelectedIsDigitalProject() throws IOException {
        expectedContentFileLocation = "translation-form-request/SelectBoxCheckTranslationRequestForm.txt";
        generatedContentOutputFile = "TranslationFormRequestProjectSelectedIsDigitalProject.";

        request = DocmosisTranslationRequest.builder()
            .project(DocmosisWelshProject.builder()
                .digitalProject(true)
                .build())
            .build();

        DocmosisDocument docmosisDocumentPDF = underTest.buildTranslationRequestDocuments(request.toBuilder()
            .format(RenderFormat.PDF)
            .build()
        );

        generateWordDocument();

        assertThat(remove(extractPdfContent(docmosisDocumentPDF.getBytes())))
            .isEqualToNormalizingWhitespace(getExpectedText(expectedContentFileLocation));
    }

    @Test
    void shouldGenerateTranslationFormRequestProjectSelectedIsCtsc() throws IOException {
        expectedContentFileLocation = "translation-form-request/SelectBoxCheckTranslationRequestForm.txt";
        generatedContentOutputFile = "TranslationFormRequestProjectSelectedIsCtsc.";

        request = DocmosisTranslationRequest.builder()
            .project(DocmosisWelshProject.builder()
                .ctsc(true)
                .build())
            .build();

        DocmosisDocument docmosisDocumentPDF = underTest.buildTranslationRequestDocuments(request.toBuilder()
            .format(RenderFormat.PDF)
            .build()
        );

        generateWordDocument();

        assertThat(remove(extractPdfContent(docmosisDocumentPDF.getBytes())))
            .isEqualToNormalizingWhitespace(getExpectedText(expectedContentFileLocation));
    }

    @Test
    void shouldGenerateTranslationFormRequestLayoutIsBilingual() throws IOException {
        expectedContentFileLocation = "translation-form-request/SelectBoxCheckTranslationRequestForm.txt";
        generatedContentOutputFile = "TranslationFormRequestLayoutIsBilingual.";

        request = DocmosisTranslationRequest.builder()
            .layout(DocmosisWelshLayout.builder()
                .bilingual(true)
                .build())
            .build();

        DocmosisDocument docmosisDocumentPDF = underTest.buildTranslationRequestDocuments(request.toBuilder()
            .format(RenderFormat.PDF)
            .build()
        );

        generateWordDocument();

        assertThat(remove(extractPdfContent(docmosisDocumentPDF.getBytes())))
            .isEqualToNormalizingWhitespace(getExpectedText(expectedContentFileLocation));
    }

    @Test
    void shouldGenerateTranslationFormRequestLayoutIsOther() throws IOException {
        expectedContentFileLocation = "translation-form-request/SelectBoxCheckTranslationRequestForm.txt";
        generatedContentOutputFile = "TranslationFormRequestLayoutIsOther.";

        request = DocmosisTranslationRequest.builder()
            .layout(DocmosisWelshLayout.builder()
                .other(true)
                .build())
            .build();

        DocmosisDocument docmosisDocumentPDF = underTest.buildTranslationRequestDocuments(request.toBuilder()
            .format(RenderFormat.PDF)
            .build()
        );

        generateWordDocument();

        assertThat(remove(extractPdfContent(docmosisDocumentPDF.getBytes())))
            .isEqualToNormalizingWhitespace(getExpectedText(expectedContentFileLocation));
    }

    private void assertActualOutputMatchesTestFile() {
        assertThat(remove(extractPdfContent(getDocmosisDocumentPDFBytes())))
            .isEqualToNormalizingWhitespace(getExpectedText(expectedContentFileLocation));
    }

    private byte[] getDocmosisDocumentPDFBytes(){
        return underTest.buildTranslationRequestDocuments(
            request.toBuilder()
                .format(RenderFormat.PDF)
                .build()
        ).getBytes();
    }

    private DocmosisTranslationRequest buildTranslationRequest(boolean isEnglishToWelsh,
                                                               DocmosisDocument orderDocumentToTranslate) {
        return DocmosisTranslationRequest.builder()
            .name(NAME)
            .department(DEPARTMENT)
            .contactInformation(CONTACT_INFORMATION)
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
            .project(DocmosisWelshProject.builder()
                .reform(true)
                .build())
            .description("Translation of " + orderDocumentToTranslate.getDocumentTitle())
            .layout(DocmosisWelshLayout.builder()
                .mirrorImage(true)
                .build())
            .translate(DocmosisTranslateLanguages.builder()
                .englishToWelsh(isEnglishToWelsh)
                .welshToEnglish(!isEnglishToWelsh)
                .build())
            .wordCount(countWords(convertByteToString(orderDocumentToTranslate.getBytes())))
            .dateOfReturn(formatDate(FIXED_DATE))
            .build();
    }

    private String formatDate(LocalDate date) {
        return formatLocalDateToString(date, DATE);
    }

    private void generateWordDocument() throws IOException {
        storeToOuputFolder(
            generatedContentOutputFile.concat(RenderFormat.WORD.getExtension()),
            underTest.buildTranslationRequestDocuments(request.toBuilder()
                .format(RenderFormat.WORD)
                .build()
            ).getBytes()
        );
    }

    private static int countWords(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return text.split("\\s", -1).length;
    }

    private static String convertByteToString(byte[] byteValue) {
        return ("" + Arrays.toString(byteValue));
    }

    private static String getExpectedText(String fileName) {
        try {
            return readString(fileName);
        } catch (Exception e) {
            throw new RuntimeException("Missing assertion text for order. Please create a filename named " + fileName);
        }
    }
}
