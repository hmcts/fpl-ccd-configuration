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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseSubmission;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C110A;
import static uk.gov.hmcts.reform.fpl.service.casesubmission.SampleCaseSubmissionTestDataHelper.expectedDocmosisCaseSubmission;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CaseSubmissionService.class, JacksonAutoConfiguration.class})
public class CaseSubmissionServiceTest {
    private static final byte[] PDF = {1, 2, 3, 4, 5};

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private DocmosisDocumentGeneratorService documentGeneratorService;

    @MockBean
    private CaseSubmissionTemplateDataGenerationService templateDataGenerationService;

    @Captor
    private ArgumentCaptor<DocmosisCaseSubmission> caseSubmissionDataCaptor;

    @Autowired
    private CaseSubmissionService caseSubmissionService;

    private CaseDetails givenCaseDetails;
    private DocmosisCaseSubmission expectedCaseSubmissionWithCaseNumber;

    @BeforeEach
    void setup() {
        DocmosisCaseSubmission expectedCaseSubmission = expectedDocmosisCaseSubmission();
        given(templateDataGenerationService.getTemplateData(any())).willReturn(expectedCaseSubmission);

        expectedCaseSubmissionWithCaseNumber = expectedCaseSubmission.toBuilder().caseNumber("12345").build();
        given(templateDataGenerationService.populateDocmosisCaseSubmissionWithCaseNumber(
            expectedCaseSubmission, 12345L)).willReturn(expectedCaseSubmissionWithCaseNumber);

        given(documentGeneratorService.generateDocmosisDocument(any(DocmosisData.class), any()))
            .willReturn(new DocmosisDocument("case_submission_c110a.pdf", PDF));

        given(uploadDocumentService.uploadPDF(any(), any())).willReturn(document());

        givenCaseDetails = populatedCaseDetails();
    }

    @Test
    void shouldGenerateCaseSubmissionDocumentSuccessfully() {
        caseSubmissionService.generateSubmittedFormPDF(givenCaseDetails);

        verify(documentGeneratorService).generateDocmosisDocument(caseSubmissionDataCaptor.capture(), eq(C110A));
        DocmosisCaseSubmission caseSubmission = caseSubmissionDataCaptor.getValue();
        assertThat(caseSubmission).isEqualTo(expectedCaseSubmissionWithCaseNumber);

        verify(uploadDocumentService).uploadPDF(eq(PDF), any());
    }
}
