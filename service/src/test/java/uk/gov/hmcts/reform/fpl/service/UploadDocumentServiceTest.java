package uk.gov.hmcts.reform.fpl.service;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.successfulDocumentUploadResponse;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.unsuccessfulDocumentUploadResponse;

@RunWith(SpringRunner.class)
public class UploadDocumentServiceTest {

    private static final String USER_ID = "1";
    private static final String AUTHORIZATION_TOKEN = "Bearer token";
    private static final String SERVICE_AUTHORIZATION_TOKEN = "Bearer service token";

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    protected DocumentUploadClientApi documentUploadClient;

    @InjectMocks
    private UploadDocumentService uploadDocumentService;

    @Before
    public void setup() {
        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTHORIZATION_TOKEN);
    }

    @Test
    public void testSuccessfulResponseReturnsDocument() throws IOException {
        UploadResponse request = successfulDocumentUploadResponse();

        given(documentUploadClient.upload(eq(AUTHORIZATION_TOKEN), eq(SERVICE_AUTHORIZATION_TOKEN), eq(USER_ID), any()))
            .willReturn(request);

        Document document = uploadDocumentService.upload(USER_ID, AUTHORIZATION_TOKEN, new byte[0], "file");

        Assertions.assertThat(document).isEqualTo(request.getEmbedded().getDocuments().get(0));
    }

    @Test
    public void testUnsuccessfulResponseThrowsRuntimeException() throws IOException {
        given(documentUploadClient.upload(eq(AUTHORIZATION_TOKEN), eq(SERVICE_AUTHORIZATION_TOKEN), eq(USER_ID), any()))
            .willReturn(unsuccessfulDocumentUploadResponse());

        assertThatThrownBy(() -> uploadDocumentService.upload(USER_ID, AUTHORIZATION_TOKEN, new byte[0], "file"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Document upload failed due to empty result");
    }
}
