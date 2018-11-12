package uk.gov.hmcts.reform.fpl.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.Document;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.utils.ResourceLoader.successfulDocumentManagementUploadResponse;
import static uk.gov.hmcts.reform.fpl.utils.ResourceLoader.unsuccessfulDocumentManagementUploadResponse;

@RunWith(SpringRunner.class)
public class UploadDocumentServiceTest {

    private static final String AUTHORIZATION_TOKEN = "Bearer token";

    @Mock
    protected DocumentUploadClientApi documentUploadClient;

    @InjectMocks
    private UploadDocumentService uploadDocumentService = new UploadDocumentService();

    @Test
    public void testSuccessfulResponseReturnsDocument() throws IOException {
        given(documentUploadClient.upload(eq(AUTHORIZATION_TOKEN), any(), any(), any()))
            .willReturn(successfulDocumentManagementUploadResponse());

        assertThat("Method should provide document on success",
            Document.class.isInstance(uploadDocumentService.uploadDocument("1",
            AUTHORIZATION_TOKEN, "1", new byte[]{1, 2, 3, 4}, "test")));
    }

    @Test(expected = RuntimeException.class)
    public void testUnsuccessfulResponseThrowsRuntimeException() throws IOException {
        given(documentUploadClient.upload(eq(AUTHORIZATION_TOKEN), any(), any(), any()))
            .willReturn(unsuccessfulDocumentManagementUploadResponse());
        uploadDocumentService.uploadDocument("1", AUTHORIZATION_TOKEN, "1",
            new byte[]{1, 2, 3, 4}, "test");
    }

    @Test
    public void testUnsuccessfulResponseThrowsErrorMessage() throws IOException {
        given(documentUploadClient.upload(eq(AUTHORIZATION_TOKEN), any(), any(), any()))
            .willReturn(unsuccessfulDocumentManagementUploadResponse());
        String messsage = null;
        try {
            uploadDocumentService.uploadDocument("1", AUTHORIZATION_TOKEN, "1",
                new byte[]{1, 2, 3, 4}, "test");
        } catch (RuntimeException e) {
            messsage = e.getMessage();
        }
        assertThat("Error", messsage, is("Document management failed uploading"));
    }
}
