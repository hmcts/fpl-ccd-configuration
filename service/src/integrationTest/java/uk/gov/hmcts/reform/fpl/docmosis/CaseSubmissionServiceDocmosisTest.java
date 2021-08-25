package uk.gov.hmcts.reform.fpl.docmosis;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseData;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;

@ContextConfiguration(classes = {
    CaseSubmissionService.class, DocmosisDocumentGeneratorService.class, CaseSubmissionGenerationService.class,
    DocmosisHelper.class, FixedTimeConfiguration.class, CaseSubmissionDocumentAnnexGenerator.class
})
@SpyBeans({@SpyBean(DocmosisDocumentGeneratorService.class)})
@MockBeans({
    @MockBean(UploadDocumentService.class), @MockBean(CourtService.class), @MockBean(UserService.class),
    @MockBean(Time.class)
})
class CaseSubmissionServiceDocmosisTest extends AbstractDocmosisTest {
    private static final String LA_COURT = "La Court";
    private static final String USER_NAME = "user name";
    private static final LocalDate NOW = LocalDate.of(12, 12, 12);
    private static final String EXPECTED_TEXT_PATH = "c110a.txt";

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
        caseData = populatedCaseData().toBuilder().languageRequirement("Yes").build();

        when(user.getUserName()).thenReturn(USER_NAME);
        when(courtService.getCourtName(any())).thenReturn(LA_COURT);
        when(time.now()).thenReturn(LocalDateTime.of(NOW, LocalTime.NOON));
    }

    @Test
    void testC110a() throws IOException {
        doAnswer(resultsCaptor).when(generatorService).generateDocmosisDocument(anyMap(), any(), any(), any());

        underTest.generateSubmittedFormPDF(caseData, false);

        String c110aContent = getPdfContent();

        String expectedText = getExpectedText();

        assertThat(c110aContent).isEqualToNormalizingWhitespace(expectedText);
    }

    private String getPdfContent() throws IOException {
        DocmosisDocument docmosisDocument = resultsCaptor.getResult();

        byte[] bytes = docmosisDocument.getBytes();

        storeToOuputFolder("c110a.pdf", bytes);

        String text = docmosisHelper.extractPdfContent(bytes);
        return docmosisHelper.remove(text, "C110A");
    }

    private String getExpectedText() {
        return readString(EXPECTED_TEXT_PATH);
    }
}
