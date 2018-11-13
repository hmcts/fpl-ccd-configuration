package uk.gov.hmcts.reform.fpl.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.templates.DocumentTemplates;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;

import static uk.gov.hmcts.reform.fpl.utils.ResourceLoader.emptyCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.ResourceLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.ResourceLoader.successfulCallBack;
import static uk.gov.hmcts.reform.fpl.utils.ResourceLoader.successfulDocumentUpload;
import static uk.gov.hmcts.reform.fpl.utils.ResourceLoader.successfulStartEventResponse;

@RunWith(SpringRunner.class)
public class CaseServiceTest {

    public static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private DocumentGeneratorService documentGeneratorService;
    @Mock
    private UploadDocumentService uploadDocumentService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @InjectMocks
    private CaseService caseService = new CaseService();
    private Method getFileName;
    private SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent
        (successfulCallBack(), "Bearer token", "1");

    public CaseServiceTest() throws IOException {
    }


    @Before
    public void setup() throws NoSuchMethodException {
        getFileName = caseService.getClass().getDeclaredMethod("getFileName", CaseDetails.class);
        getFileName.setAccessible(true);
    }

    @Test
    public void testGetFileNameReturnsCaseReferenceWhenNoTitleIsProvided()
        throws IOException, InvocationTargetException, IllegalAccessException {
        CaseDetails caseDetails = emptyCaseDetails();
        String fileName = (String) getFileName.invoke(caseService, caseDetails);

        assertThat("File name should match the caseID of 123", fileName, is("123.pdf"));
    }

    @Test
    public void testGetFileNameReturnsCaseTitleWhenProvided()
        throws IOException, InvocationTargetException, IllegalAccessException {
        CaseDetails caseDetails = populatedCaseDetails();
        String fileName = (String) getFileName.invoke(caseService, caseDetails);

        assertThat("File name should match the file name of test", fileName, is("test.pdf"));
    }

    @Test
    public void testHandleCaseSubmissionProcessesSubmittedCaseEventSuccessfully() throws IOException {
        mockSuccessfully();
        caseService.handleCaseSubmission(submittedCaseEvent);
    }


    public void mockSuccessfully() throws IOException {
        given(documentGeneratorService.documentGenerator(any())).willReturn(new byte[]{1, 2, 3});
        given(uploadDocumentService.uploadDocument(any(), any(), eq(AUTH_TOKEN),
            any(), any())).willReturn(successfulDocumentUpload());
        given(authTokenGenerator.generate()).willReturn(AUTH_TOKEN);
        given(coreCaseDataApi.startEventForCaseWorker(any(), eq(AUTH_TOKEN),
            any(), any(), any(), any(), any())).willReturn(successfulStartEventResponse());
    }
}
