package uk.gov.hmcts.reform.fpl.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.emptyCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.successfulStartEventResponse;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@RunWith(SpringRunner.class)
public class SubmittedCaseEventHandlerTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private DocumentGeneratorService documentGeneratorService;
    @Mock
    private UploadDocumentService uploadDocumentService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @InjectMocks
    private SubmittedCaseEventHandler submittedCaseEventHandler = new SubmittedCaseEventHandler();
    private Method getFileName;
    private SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(
        callbackRequest(), "Bearer token", "1"
    );

    public SubmittedCaseEventHandlerTest() throws IOException {
        // NO-OP
    }


    @Before
    public void setup() throws NoSuchMethodException {
        getFileName = submittedCaseEventHandler.getClass().getDeclaredMethod("getFileName", CaseDetails.class);
        getFileName.setAccessible(true);
    }

    @Test
    public void testGetFileNameReturnsCaseReferenceWhenNoTitleIsProvided()
        throws IOException, InvocationTargetException, IllegalAccessException {
        CaseDetails caseDetails = emptyCaseDetails();
        String fileName = (String) getFileName.invoke(submittedCaseEventHandler, caseDetails);

        assertThat("File name should match the caseID of 123", fileName, is("123.pdf"));
    }

    @Test
    public void testGetFileNameReturnsCaseTitleWhenProvided()
        throws IOException, InvocationTargetException, IllegalAccessException {
        CaseDetails caseDetails = populatedCaseDetails();
        String fileName = (String) getFileName.invoke(submittedCaseEventHandler, caseDetails);

        assertThat("File name should match the file name of test", fileName, is("test.pdf"));
    }

    @Test
    public void testHandleCaseSubmissionProcessesSubmittedCaseEventSuccessfully() throws IOException {
        mockSuccessfully();
        submittedCaseEventHandler.handleCaseSubmission(submittedCaseEvent);
    }


    public void mockSuccessfully() throws IOException {
        given(documentGeneratorService.generateSubmittedFormPDF(any())).willReturn(new byte[]{1, 2, 3});
        given(uploadDocumentService.uploadDocument(any(), any(), eq(AUTH_TOKEN),
            any(), any())).willReturn(document());
        given(authTokenGenerator.generate()).willReturn(AUTH_TOKEN);
        given(coreCaseDataApi.startEventForCaseWorker(any(), eq(AUTH_TOKEN),
            any(), any(), any(), any(), any())).willReturn(successfulStartEventResponse());
    }
}
