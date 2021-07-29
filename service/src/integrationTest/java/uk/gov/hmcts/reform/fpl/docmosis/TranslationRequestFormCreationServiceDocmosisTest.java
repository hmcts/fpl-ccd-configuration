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

    private static final String CCD_ID = "1234-5678-9012-3456";
    private static final String NAME = "Courts and Tribunals Service Centre";
    private static final String DEPARTMENT = "Family Public Law";
    private static final String CONTACT_INFORMATION = "contactfpl@justice.gov.uk";
    private static final String FAMILY_MAN_CASE_NUMBER = "FamilyManCaseNumber123";
    private static final LocalDate FIXED_DATE = LocalDate.of(2021, 7, 28);
    private static final int WORD_COUNT = 2034;
    private final byte[] pdf = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    private final DocmosisDocument orderDocumentToTranslate = new DocmosisDocument("example.pdf", pdf);

    @Autowired
    private TranslationRequestFormCreationService underTest;

    @Test
    void shouldGenerateTranslationFormRequestEnglishToWelsh() throws IOException {
        String expectedContentFileLocation = "translation-form-request/EnglishToWelshTranslationDocument.txt";
        String generatedContentOutputFile = "EnglishToWelshTranslationDocument.";

        DocmosisTranslationRequest request = buildTranslationRequest(true, orderDocumentToTranslate);

        generateWordDocument(request, generatedContentOutputFile);

        assertActualOutputMatchesTestFile(request, expectedContentFileLocation);
    }

    @Test
    void shouldGenerateTranslationFormRequestWelshToEnglish() throws IOException {
        String expectedContentFileLocation = "translation-form-request/WelshToEnglishTranslationDocument.txt";
        String generatedContentOutputFile = "WelshToEnglishTranslationDocument.";

        DocmosisTranslationRequest request = buildTranslationRequest(false, orderDocumentToTranslate);

        generateWordDocument(request, generatedContentOutputFile);

        assertActualOutputMatchesTestFile(request, expectedContentFileLocation);
    }

    @Test
    void shouldGenerateTranslationFormRequestProjectSelectedIsDigitalProject() throws IOException {
        String expectedContentFileLocation = "translation-form-request/SelectBoxCheckTranslationRequestForm.txt";
        String generatedContentOutputFile = "TranslationFormRequestProjectSelectedIsDigitalProject.";

        DocmosisTranslationRequest request = DocmosisTranslationRequest.builder()
            .project(DocmosisWelshProject.builder()
                .digitalProject(true)
                .build())
            .build();

        DocmosisDocument docmosisDocumentPDF = underTest.buildTranslationRequestDocuments(request.toBuilder()
            .format(RenderFormat.PDF)
            .build()
        );

        generateWordDocument(request, generatedContentOutputFile);

        assertThat(remove(extractPdfContent(docmosisDocumentPDF.getBytes())))
            .isEqualToNormalizingWhitespace(getExpectedText(expectedContentFileLocation));
    }

    @Test
    void shouldGenerateTranslationFormRequestProjectSelectedIsCtsc() throws IOException {
        String expectedContentFileLocation = "translation-form-request/SelectBoxCheckTranslationRequestForm.txt";
        String generatedContentOutputFile = "TranslationFormRequestProjectSelectedIsCtsc.";

        DocmosisTranslationRequest request = DocmosisTranslationRequest.builder()
            .project(DocmosisWelshProject.builder()
                .ctsc(true)
                .build())
            .build();

        DocmosisDocument docmosisDocumentPDF = underTest.buildTranslationRequestDocuments(request.toBuilder()
            .format(RenderFormat.PDF)
            .build()
        );

        generateWordDocument(request, generatedContentOutputFile);

        assertThat(remove(extractPdfContent(docmosisDocumentPDF.getBytes())))
            .isEqualToNormalizingWhitespace(getExpectedText(expectedContentFileLocation));
    }

    @Test
    void shouldGenerateTranslationFormRequestLayoutIsBilingual() throws IOException {
        String expectedContentFileLocation = "translation-form-request/SelectBoxCheckTranslationRequestForm.txt";
        String generatedContentOutputFile = "TranslationFormRequestLayoutIsBilingual.";

        DocmosisTranslationRequest request = DocmosisTranslationRequest.builder()
            .layout(DocmosisWelshLayout.builder()
                .bilingual(true)
                .build())
            .build();

        DocmosisDocument docmosisDocumentPDF = underTest.buildTranslationRequestDocuments(request.toBuilder()
            .format(RenderFormat.PDF)
            .build()
        );

        generateWordDocument(request, generatedContentOutputFile);

        assertThat(remove(extractPdfContent(docmosisDocumentPDF.getBytes())))
            .isEqualToNormalizingWhitespace(getExpectedText(expectedContentFileLocation));
    }

    @Test
    void shouldGenerateTranslationFormRequestLayoutIsOther() throws IOException {
        String expectedContentFileLocation = "translation-form-request/SelectBoxCheckTranslationRequestForm.txt";
        String generatedContentOutputFile = "TranslationFormRequestLayoutIsOther.";

        DocmosisTranslationRequest request = DocmosisTranslationRequest.builder()
            .layout(DocmosisWelshLayout.builder()
                .other(true)
                .build())
            .build();

        DocmosisDocument docmosisDocumentPDF = underTest.buildTranslationRequestDocuments(request.toBuilder()
            .format(RenderFormat.PDF)
            .build()
        );

        generateWordDocument(request, generatedContentOutputFile);

        assertThat(remove(extractPdfContent(docmosisDocumentPDF.getBytes())))
            .isEqualToNormalizingWhitespace(getExpectedText(expectedContentFileLocation));
    }

    private void assertActualOutputMatchesTestFile(DocmosisTranslationRequest request,
                                                   String expectedContentFileLocation) {
        assertThat(remove(extractPdfContent(getDocmosisDocumentPDFBytes(request))))
            .isEqualToNormalizingWhitespace(getExpectedText(expectedContentFileLocation));
    }

    private byte[] getDocmosisDocumentPDFBytes(DocmosisTranslationRequest request) {
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
            .ccdId(CCD_ID)
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
            .wordCount(WORD_COUNT)
            .dateOfReturn(formatDate(FIXED_DATE))
            .build();
    }

    private String formatDate(LocalDate date) {
        return formatLocalDateToString(date, DATE);
    }

    private void generateWordDocument(DocmosisTranslationRequest request,
                                      String generatedContentOutputFile) throws IOException {
        storeToOuputFolder(
            generatedContentOutputFile.concat(RenderFormat.WORD.getExtension()),
            underTest.buildTranslationRequestDocuments(request.toBuilder()
                .format(RenderFormat.WORD)
                .build()
            ).getBytes()
        );
    }

    private static String getExpectedText(String fileName) {
        try {
            return readString(fileName);
        } catch (Exception e) {
            throw new RuntimeException("Missing assertion text for order. Please create a filename named " + fileName);
        }
    }
}
