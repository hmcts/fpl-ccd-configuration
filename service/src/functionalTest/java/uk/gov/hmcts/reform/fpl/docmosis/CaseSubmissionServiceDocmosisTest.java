package uk.gov.hmcts.reform.fpl.docmosis;

import org.apache.commons.text.StringSubstitutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.mock.mockito.SpyBeans;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.group.C110A;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.casesubmission.CaseSubmissionService;
import uk.gov.hmcts.reform.fpl.service.docmosis.CaseSubmissionDocumentAnnexGenerator;
import uk.gov.hmcts.reform.fpl.service.docmosis.CaseSubmissionGenerationService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.model.configuration.Language.ENGLISH;
import static uk.gov.hmcts.reform.fpl.model.configuration.Language.WELSH;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseData;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;

@ContextConfiguration(classes = {
    CaseSubmissionService.class, DocmosisDocumentGeneratorService.class, CaseSubmissionGenerationService.class,
    DocmosisHelper.class, FixedTimeConfiguration.class, CaseSubmissionDocumentAnnexGenerator.class
})
@SpyBeans(value = {@SpyBean(DocmosisDocumentGeneratorService.class)})
@MockBeans(value = {
    @MockBean(UploadDocumentService.class), @MockBean(CourtService.class), @MockBean(UserService.class),
    @MockBean(Time.class)
})
class CaseSubmissionServiceDocmosisTest extends AbstractDocmosisTest {
    private static final String LA_COURT = "La Court";
    private static final String USER_NAME = "user name";
    private static final LocalDate NOW = LocalDate.of(12, 12, 12);

    @Autowired
    private Time time;
    @Autowired
    private DocmosisDocumentGeneratorService generatorService;
    @Autowired
    private UserService user;
    @Autowired
    private CourtService courtService;
    @Autowired
    private DocmosisHelper docmosisHelper;
    @Autowired
    private CaseSubmissionService underTest;

    private CaseData caseData;

    @BeforeEach
    void setUp() {

        when(user.getUserName()).thenReturn(USER_NAME);
        when(courtService.getCourtName(any())).thenReturn(LA_COURT);
        when(time.now()).thenReturn(LocalDateTime.of(NOW, LocalTime.NOON));
    }

    @Test
    void testC110a() throws IOException {

        caseData = populatedCaseData().toBuilder()
            .languageRequirement("Yes")
            .c110A(C110A.builder()
                .languageRequirementApplication(ENGLISH)
                .build())
            .build();

        doAnswer(resultsCaptor).when(generatorService).generateDocmosisDocument(anyMap(), any(), any(), any());

        underTest.generateSubmittedFormPDF(caseData, false);

        String c110aContent = getPdfContent("c110a.pdf");

        String expectedText = readString("c110a.txt");
        expectedText = StringSubstitutor.replace(expectedText, Map.of("languageRequirement", "Yes"));

        assertThat(c110aContent).isEqualToNormalizingWhitespace(expectedText);
    }

    @Test
    void testC110aNoLanguageRequirement() throws IOException {

        caseData = populatedCaseData().toBuilder()
            .c110A(C110A.builder()
                .build())
            .build();

        doAnswer(resultsCaptor).when(generatorService).generateDocmosisDocument(anyMap(), any(), any(), any());

        underTest.generateSubmittedFormPDF(caseData, false);

        String c110aContent = getPdfContent("c110a.pdf");

        String expectedText = readString("c110a.txt");
        expectedText = StringSubstitutor.replace(expectedText, Map.of("languageRequirement", "No"));

        assertThat(c110aContent).isEqualToNormalizingWhitespace(expectedText);
    }

    @Test
    void testC110aWelsh() throws IOException {

        caseData = populatedCaseData().toBuilder()
            .languageRequirement("Yes")
            .c110A(C110A.builder()
                .languageRequirementApplication(WELSH)
                .build())
            .build();


        doAnswer(resultsCaptor).when(generatorService).generateDocmosisDocument(anyMap(), any(), any(), any());

        underTest.generateSubmittedFormPDF(caseData, false);

        String c110aContent = getPdfContent("c110a-Welsh.pdf");

        String expectedText = readString("c110a-Welsh.txt");

        assertThat(c110aContent).isEqualToNormalizingWhitespace(expectedText);
    }

    private String getPdfContent(String fileName) throws IOException {
        DocmosisDocument docmosisDocument = resultsCaptor.getResult();

        byte[] bytes = docmosisDocument.getBytes();

        storeToOuputFolder(fileName, bytes);

        String text = docmosisHelper.extractPdfContent(bytes);
        return docmosisHelper.remove(text, "C110A");
    }

}
