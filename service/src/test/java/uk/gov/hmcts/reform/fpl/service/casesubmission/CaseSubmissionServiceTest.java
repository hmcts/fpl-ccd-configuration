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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseSubmission;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.CaseSubmissionGenerationService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C110A;
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

    @Autowired
    private CaseSubmissionService caseSubmissionService;

    private CaseData givenCaseData;
    private DocmosisCaseSubmission expectedCaseSubmission;

    @BeforeEach
    void setup() {
        expectedCaseSubmission = expectedDocmosisCaseSubmission();
        given(templateDataGenerationService.getTemplateData(any())).willReturn(expectedCaseSubmission);

        given(documentGeneratorService.generateDocmosisDocument(any(DocmosisData.class), any()))
            .willReturn(new DocmosisDocument("case_submission_c110a.pdf", PDF));

        given(uploadDocumentService.uploadPDF(any(), any())).willReturn(document());

        givenCaseData = populatedCaseData();
    }

    @Test
    void shouldGenerateCaseSubmissionDocumentSuccessfully() {
        caseSubmissionService.generateSubmittedFormPDF(givenCaseData, false);

        verify(documentGeneratorService).generateDocmosisDocument(caseSubmissionDataCaptor.capture(), eq(C110A));
        DocmosisCaseSubmission caseSubmission = caseSubmissionDataCaptor.getValue();
        assertThat(caseSubmission).isEqualTo(expectedCaseSubmission);

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
