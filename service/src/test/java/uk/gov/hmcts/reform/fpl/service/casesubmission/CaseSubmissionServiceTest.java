package uk.gov.hmcts.reform.fpl.service.casesubmission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
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

    @Autowired
    private CaseSubmissionService caseSubmissionService;

    private CaseDetails givenCaseDetails;

    @BeforeEach
    void setup() {
        DocmosisDocument docmosisDocument = new DocmosisDocument("case_submission_c110a.pdf", PDF);

        DocmosisCaseSubmission expectedCaseSubmission = expectedDocmosisCaseSubmission();
        given(templateDataGenerationService.getTemplateData(any())).willReturn(expectedCaseSubmission);

        given(documentGeneratorService.generateDocmosisDocument(any(DocmosisData.class), any()))
            .willReturn(docmosisDocument);

        given(uploadDocumentService.uploadPDF(any(), any())).willReturn(document());

        givenCaseDetails = populatedCaseDetails();
    }

    @Test
    void shouldGenerateCaseSubmissionDocumentSuccessfully() {
        caseSubmissionService.generateSubmittedFormPDF(givenCaseDetails);

        verify(uploadDocumentService).uploadPDF(eq(PDF), any());
    }
}
