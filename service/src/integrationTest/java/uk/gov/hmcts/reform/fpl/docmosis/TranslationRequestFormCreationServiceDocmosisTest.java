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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.docmosis.DocmosisHelper.extractPdfContent;
import static uk.gov.hmcts.reform.fpl.docmosis.DocmosisHelper.remove;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;

@ContextConfiguration(classes = {
    TranslationRequestFormCreationService.class,
    DocmosisDocumentGeneratorService.class
})

public class TranslationRequestFormCreationServiceDocmosisTest extends AbstractDocmosisTest {

    @Autowired
    private TranslationRequestFormCreationService underTest;

    private DocmosisTranslationRequest request;
    private String expectedContentFileLocation;
    private String generatedContentOutputFile;

    @Test
    void shouldGenerateTranslationRequestForm_EnglishTo_Welsh_PDF() throws IOException {
        expectedContentFileLocation = "translation-form-request/EnglishToWelshTranslationDocument.txt";
        generatedContentOutputFile = "EnglishToWelshTranslationDocument.";

        request = buildTranslationRequest(true, "", null);

        DocmosisDocument docmosisDocumentPDF = underTest.buildTranslationRequestDocuments(request.toBuilder()
            .format(RenderFormat.PDF)
            .build()
        );

        generateDocuments(docmosisDocumentPDF);

        assertThat(remove(extractPdfContent(docmosisDocumentPDF.getBytes())))
            .isEqualToNormalizingWhitespace(getExpectedText(expectedContentFileLocation));
    }

    @Test
    void shouldGenerateTranslationRequestForm_WelshTo_English_PDF() throws IOException {
        expectedContentFileLocation = "translation-form-request/WelshToEnglishTranslationDocument.txt";
        generatedContentOutputFile = "WelshToEnglishTranslationDocument.";

        request = buildTranslationRequest(false, "", null);

        DocmosisDocument docmosisDocumentPDF = underTest.buildTranslationRequestDocuments(request.toBuilder()
            .format(RenderFormat.PDF)
            .build()
        );

        generateDocuments(docmosisDocumentPDF);

        assertThat(remove(extractPdfContent(docmosisDocumentPDF.getBytes())))
            .isEqualToNormalizingWhitespace(getExpectedText(expectedContentFileLocation));
    }

    private DocmosisTranslationRequest buildTranslationRequest(boolean isEnglishToWelsh,
                                                               String filepathToCountWords, LocalDate dateOfReturn) {
        return DocmosisTranslationRequest.builder()
            .familyManCaseNumber("FamilyManCaseNumber123")
            .name("Family Public Law System")
            .description("Test description.".repeat(2))
            .contactInformation("contactfpl@justice.gov.uk")
            .department("Family Public Law")
            .project(DocmosisWelshProject.builder()
                .reform(true)
                .build())
            .layout(DocmosisWelshLayout.builder()
                .mirrorImage(true)
                .build())
            .translate(DocmosisTranslateLanguages.builder()
                .englishToWelsh(isEnglishToWelsh)
                .welshToEnglish(!isEnglishToWelsh)
                .build())
            .wordCount(countWordsInFile(filepathToCountWords))
            .dateOfReturn(dateOfReturn)
            .build();
    }

    private void generateDocuments(
        DocmosisDocument docmosisDocumentPDF) throws IOException {

        storeToOuputFolder(
            generatedContentOutputFile.concat(RenderFormat.PDF.getExtension()),
            docmosisDocumentPDF.getBytes()
        );

        storeToOuputFolder(
            generatedContentOutputFile.concat(RenderFormat.WORD.getExtension()),
            underTest.buildTranslationRequestDocuments(request.toBuilder()
                .format(RenderFormat.WORD)
                .build()
            ).getBytes()
        );
    }

    private int countWordsInFile(String testFileLocation) {
        String line;
        int numberOfWords = 0;

        try{
            FileReader file = new FileReader(testFileLocation);
            BufferedReader bufferedReader = new BufferedReader(file);

            while((line = bufferedReader.readLine()) != null) {
                String[] words = line.split(" ");
                numberOfWords = numberOfWords + words.length;
            }
            bufferedReader.close();
        } catch (IOException exception) {
           return 0;
        }

        return numberOfWords;
    }

    private String getExpectedText(String fileName) {
        try {
            return readString(fileName);
        } catch (Exception e) {
            throw new RuntimeException("Missing assertion text for order. Please create a "
                + "filename named " + fileName);
        }
    }
}
