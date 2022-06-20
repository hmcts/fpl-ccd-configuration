package uk.gov.hmcts.reform.fpl.service.casesubmission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisC16Supplement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseSubmission;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.CaseSubmissionGenerationService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C110A;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C16_SUPPLEMENT;
import static uk.gov.hmcts.reform.fpl.service.casesubmission.SampleCaseSubmissionTestDataHelper.expectedDocmosisC16Supplement;
import static uk.gov.hmcts.reform.fpl.service.casesubmission.SampleCaseSubmissionTestDataHelper.expectedDocmosisCaseSubmission;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseData;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CaseSubmissionService.class, JacksonAutoConfiguration.class})
class CaseSubmissionServiceTest {
    private static final byte[] PDF = TestDataHelper.DOCUMENT_CONTENT;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private DocmosisDocumentGeneratorService documentGeneratorService;

    @MockBean
    private CaseSubmissionGenerationService templateDataGenerationService;

    @Captor
    private ArgumentCaptor<DocmosisCaseSubmission> caseSubmissionDataCaptor;

    @Captor
    private ArgumentCaptor<DocmosisC16Supplement> caseSubmissionSupplementDataCaptor;


    @Autowired
    private CaseSubmissionService caseSubmissionService;

    private CaseData givenCaseData;
    private DocmosisCaseSubmission expectedCaseSubmission;
    private DocmosisC16Supplement expectedC16Supplement;

    @BeforeEach
    void setup() {
        expectedCaseSubmission = expectedDocmosisCaseSubmission();
        expectedC16Supplement = expectedDocmosisC16Supplement();
        given(templateDataGenerationService.getTemplateData(any())).willReturn(expectedCaseSubmission);
        given(templateDataGenerationService.getC16SupplementData(any(), anyBoolean()))
            .willReturn(expectedC16Supplement);

        given(documentGeneratorService
            .generateDocmosisDocument(any(DocmosisData.class), any(), any(), any()))
            .willReturn(new DocmosisDocument("case_submission_c110a.pdf", PDF));

        given(uploadDocumentService.uploadPDF(any(), any())).willReturn(document());

        givenCaseData = populatedCaseData();
    }

    @Test
    void shouldGenerateCaseSubmissionDocumentSuccessfullyDefault() {
        caseSubmissionService.generateC110aSubmittedFormPDF(givenCaseData, false);

        verify(documentGeneratorService).generateDocmosisDocument(caseSubmissionDataCaptor.capture(),
            eq(C110A),
            eq(RenderFormat.PDF),
            eq(Language.ENGLISH));

        DocmosisCaseSubmission caseSubmission = caseSubmissionDataCaptor.getValue();
        assertThat(caseSubmission).isEqualTo(expectedCaseSubmission);

        verify(uploadDocumentService).uploadPDF(eq(PDF), any());
    }

    @Test
    void shouldGenerateCaseSubmissionDocumentSuccessfullyIfWelsh() {
        caseSubmissionService.generateC110aSubmittedFormPDF(givenCaseData.toBuilder()
            .c110A(uk.gov.hmcts.reform.fpl.model.group.C110A.builder()
                .languageRequirementApplication(Language.WELSH)
                .build())
            .build(), false);

        verify(documentGeneratorService).generateDocmosisDocument(caseSubmissionDataCaptor.capture(),
            eq(C110A),
            eq(RenderFormat.PDF),
            eq(Language.WELSH));

        DocmosisCaseSubmission caseSubmission = caseSubmissionDataCaptor.getValue();
        assertThat(caseSubmission).isEqualTo(expectedCaseSubmission);

        verify(uploadDocumentService).uploadPDF(eq(PDF), any());
    }

    @Test
    void shouldGenerateSupplementSuccessfullyIfC1Application() {
        caseSubmissionService.generateSupplementPDF(givenCaseData, false, C16_SUPPLEMENT);

        verify(documentGeneratorService).generateDocmosisDocument(caseSubmissionSupplementDataCaptor.capture(),
            eq(C16_SUPPLEMENT),
            eq(RenderFormat.PDF),
            eq(Language.ENGLISH));

        DocmosisC16Supplement c16Supplement = caseSubmissionSupplementDataCaptor.getValue();
        assertThat(c16Supplement).isEqualTo(expectedC16Supplement);

        verify(uploadDocumentService).uploadPDF(eq(PDF), any());
    }

    @Test
    void shouldGetSigneeName() {
        final CaseData caseData = CaseData.builder().build();

        given(templateDataGenerationService.getSigneeName(caseData)).willReturn("John Smith");

        final String actualSigneeName = caseSubmissionService.getSigneeName(caseData);

        assertThat(actualSigneeName).isEqualTo("John Smith");
    }
}
