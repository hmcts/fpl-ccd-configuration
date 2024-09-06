package uk.gov.hmcts.reform.fpl.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.utils.SecureDocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.SecureDocumentManagementStoreLoader.successfulDocumentUploadResponse;

@ExtendWith(SpringExtension.class)
class SecureDocStoreServiceTest {

    private static final String USER_ID = "1";
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String SERVICE_AUTH_TOKEN = "Bearer service token";

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CaseDocumentClientApi caseDocumentClientApi;
    @Mock
    private RequestData requestData;
    @Mock
    private IdamClient idamClient;

    @Mock
    private ResponseEntity<Resource> resourceResponseEntity;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private SecureDocStoreService secureDocStoreService;

    @BeforeEach
    void setup() {
        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
        given(requestData.authorisation()).willReturn(AUTH_TOKEN);
        given(requestData.userId()).willReturn(USER_ID);
        given(featureToggleService.isSecureDocstoreEnabled()).willReturn(true);
    }

    @Test
    void shouldReturnFirstUploadedDocument() {
        UploadResponse request = successfulDocumentUploadResponse();
        given(caseDocumentClientApi.uploadDocuments(eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), any()))
            .willReturn(request);

        Document document = secureDocStoreService.uploadDocument(new byte[0], "file", "text/pdf");

        Assertions.assertThat(document).isEqualTo(request.getDocuments().get(0));
    }

    @Test
    void downloadDocument() {
        Document document = document();
        byte[] expectedDocumentContents = "test".getBytes();

        ResponseEntity<Resource> expectedResponse = ResponseEntity.ok(new ByteArrayResource(expectedDocumentContents));
        given(resourceResponseEntity.getStatusCode())
            .willReturn(expectedResponse.getStatusCode());

        given(resourceResponseEntity.getBody())
            .willReturn(expectedResponse.getBody());

        given(caseDocumentClientApi.getDocumentBinary(anyString(), anyString(), any()))
            .willReturn(resourceResponseEntity);

        byte[] documentContents = secureDocStoreService.downloadDocument(document.links.binary.href);

        assertThat(documentContents).isEqualTo(expectedDocumentContents);

        verify(caseDocumentClientApi).getDocumentBinary(AUTH_TOKEN,
            SERVICE_AUTH_TOKEN,
            UUID.fromString("85d97996-22a5-40d7-882e-3a382c8ae1b4")
        );
    }
}
