package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.handlers.SubmittedCaseEventHandler.SubmittedFormFilenameHelper;
import uk.gov.hmcts.reform.fpl.service.CaseRepository;
import uk.gov.hmcts.reform.fpl.service.DocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.emptyCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@RunWith(SpringRunner.class)
public class SubmittedCaseEventHandlerTest {

    private static final String AUTHORIZATION_TOKEN = "Bearer token";
    private static final String USER_ID = "1";

    @Mock
    private DocumentGeneratorService documentGeneratorService;
    @Mock
    private UploadDocumentService uploadDocumentService;
    @Mock
    private CaseRepository caseRepository;

    @InjectMocks
    private SubmittedCaseEventHandler submittedCaseEventHandler;

    private SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(
        callbackRequest(), AUTHORIZATION_TOKEN, USER_ID
    );

    public SubmittedCaseEventHandlerTest() throws IOException {
        // NO-OP
    }

    @Test
    public void fileNameShouldContainCaseReferenceWhenNoCaseNameIsProvided() throws IOException {
        CaseDetails caseDetails = emptyCaseDetails();

        String fileName = SubmittedFormFilenameHelper.buildFileName(caseDetails);

        assertThat(fileName).isEqualTo("123.pdf");
    }

    @Test
    public void fileNameShouldContainCaseTitleWhenProvided() throws IOException {
        CaseDetails caseDetails = populatedCaseDetails();

        String fileName = SubmittedFormFilenameHelper.buildFileName(caseDetails);

        assertThat(fileName).isEqualTo("test.pdf");
    }

    @Test
    public void testHandleCaseSubmissionProcessesSubmittedCaseEventSuccessfully() throws IOException {
        mockSuccessfully();
        submittedCaseEventHandler.handleCaseSubmission(submittedCaseEvent);
    }


    public void mockSuccessfully() throws IOException {
        given(documentGeneratorService.generateSubmittedFormPDF(any())).willReturn(new byte[]{1, 2, 3});
        given(uploadDocumentService.uploadPDF(any(), any(), any(), any())).willReturn(document());
    }
}
