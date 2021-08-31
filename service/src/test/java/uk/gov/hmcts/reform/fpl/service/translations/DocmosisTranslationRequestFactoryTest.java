package uk.gov.hmcts.reform.fpl.service.translations;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.docmosis.welshtranslation.DocmosisTranslateLanguages;
import uk.gov.hmcts.reform.fpl.model.docmosis.welshtranslation.DocmosisTranslationRequest;
import uk.gov.hmcts.reform.fpl.model.docmosis.welshtranslation.DocmosisWelshLayout;
import uk.gov.hmcts.reform.fpl.model.docmosis.welshtranslation.DocmosisWelshProject;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.ENGLISH_TO_WELSH;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.WELSH_TO_ENGLISH;

class DocmosisTranslationRequestFactoryTest {

    private static final String DOCUMENT_DESCRIPTION = "documentDescriptionXX";
    private static final byte[] DOCUMENT_CONTENT = "DocumentContent".getBytes();
    private static final String NAME = "Courts and Tribunals Service Centre";
    private static final String DEPARTMENT = "Family Public Law";
    private static final String CONTACT_INFORMATION = "contactfpl@justice.gov.uk";
    private static final long CASE_ID = 123456L;
    private static final String FAMILY_MAN_NUMBER = "familyManNumber";
    private static final String FILENAME = "file.pdf";
    private static final long WORD_COUNT = 234L;
    private static final LocalDateTime NOW = LocalDateTime.of(2012, 12, 3, 3, 4, 5);

    private final Time time = mock(Time.class);
    private final DocumentWordCounter documentWordCounter = mock(DocumentWordCounter.class);

    private final DocmosisTranslationRequestFactory underTest = new DocmosisTranslationRequestFactory(time,
        documentWordCounter);

    @Test
    void testEnglishToWelsh() {
        when(time.now()).thenReturn(NOW);
        when(documentWordCounter.count(DOCUMENT_CONTENT, FILENAME)).thenReturn(WORD_COUNT);

        DocmosisTranslationRequest actual = underTest.create(CaseData.builder()
                .id(CASE_ID)
                .familyManCaseNumber(FAMILY_MAN_NUMBER)
                .build(),
            ENGLISH_TO_WELSH,
            DOCUMENT_DESCRIPTION,
            DOCUMENT_CONTENT,
            FILENAME);

        assertThat(actual).isEqualTo(defaultExpectedRequest().toBuilder()
            .translate(DocmosisTranslateLanguages.builder().englishToWelsh(true).build())
            .build());
    }

    @Test
    void testWelshToEnglish() {
        when(time.now()).thenReturn(NOW);
        when(documentWordCounter.count(DOCUMENT_CONTENT, FILENAME)).thenReturn(WORD_COUNT);

        DocmosisTranslationRequest actual = underTest.create(CaseData.builder()
                .id(CASE_ID)
                .familyManCaseNumber(FAMILY_MAN_NUMBER)
                .build(),
            WELSH_TO_ENGLISH,
            DOCUMENT_DESCRIPTION,
            DOCUMENT_CONTENT,
            FILENAME);

        assertThat(actual).isEqualTo(defaultExpectedRequest().toBuilder()
            .translate(DocmosisTranslateLanguages.builder().welshToEnglish(true).build())
            .build());
    }

    @Test
    void tesIfCounterFails() {
        when(time.now()).thenReturn(NOW);
        when(documentWordCounter.count(DOCUMENT_CONTENT, FILENAME)).thenThrow(new RuntimeException("Boom!"));

        DocmosisTranslationRequest actual = underTest.create(CaseData.builder()
                .id(CASE_ID)
                .familyManCaseNumber(FAMILY_MAN_NUMBER)
                .build(),
            WELSH_TO_ENGLISH,
            DOCUMENT_DESCRIPTION,
            DOCUMENT_CONTENT,
            FILENAME);

        assertThat(actual).isEqualTo(defaultExpectedRequest().toBuilder()
            .wordCount(0)
            .translate(DocmosisTranslateLanguages.builder().welshToEnglish(true).build())
            .build());
    }

    private DocmosisTranslationRequest defaultExpectedRequest() {
        return DocmosisTranslationRequest.builder()
            .format(RenderFormat.WORD)
            .name(NAME)
            .department(DEPARTMENT)
            .contactInformation(CONTACT_INFORMATION)
            .ccdId(String.valueOf(CASE_ID))
            .familyManCaseNumber(FAMILY_MAN_NUMBER)
            .project(DocmosisWelshProject.builder().reform(true).build())
            .description("Translation of Document " + DOCUMENT_DESCRIPTION)
            .layout(DocmosisWelshLayout.builder()
                .mirrorImage(true)
                .build())
            .wordCount(WORD_COUNT)
            .dateOfReturn("04/12/2012")
            .build();
    }
}
