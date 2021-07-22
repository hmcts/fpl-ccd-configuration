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

    @Test
    void testTranslationRequestFormPDF() throws IOException {
        final String fileLocation = "translation-form-request/WelshToEnglishTranslationDocument.txt";

        DocmosisTranslationRequest standardRequest = DocmosisTranslationRequest.builder()
            .familyManCaseNumber("FamilyManCaseNumber123")
            .name("Family Public Law System")
            .project(DocmosisWelshProject.builder()
                .reform(true)
                .build())
            .description("We are the champions. ".repeat(10))
            .translate(DocmosisTranslateLanguages.builder().englishToWelsh(true).welshToEnglish(true).build())
            .contactInformation("contactfpl@justice.gov.uk")
            .department("Family Public Law")
            .layout(DocmosisWelshLayout.builder()
                .mirrorImage(true)
                .build())
            .wordCount(countWordsInFile(""))
            .dateOfReturn(null)
            .build();

        DocmosisDocument docmosisDocumentPDF = underTest.buildTranslationRequestDocuments(standardRequest.toBuilder()
            .format(RenderFormat.PDF)
            .build()
        );

        storeToOuputFolder(
            "WelshToEnglishTranslationDocument.pdf",
            docmosisDocumentPDF.getBytes()
        );

        storeToOuputFolder(
            "WelshToEnglishTranslationDocument.doc",
            underTest.buildTranslationRequestDocuments(standardRequest.toBuilder()
                .format(RenderFormat.WORD)
                .build()
            ).getBytes()
        );

        assertThat(remove(extractPdfContent(docmosisDocumentPDF.getBytes()))).isEqualTo(
            getExpectedText(fileLocation));
    }

    private int countWordsInFile(String fileLocation) {
        String line;
        int numberOfWords = 0;

        try{
            FileReader file = new FileReader(fileLocation);
            BufferedReader bufferedReader = new BufferedReader(file);

            while((line = bufferedReader.readLine()) != null) {
                String words[] = line.split(" ");
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
