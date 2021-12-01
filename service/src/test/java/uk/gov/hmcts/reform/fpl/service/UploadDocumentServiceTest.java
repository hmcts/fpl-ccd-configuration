package uk.gov.hmcts.reform.fpl.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.service.UploadDocumentService.oldToSecureDocument;

@ExtendWith(SpringExtension.class)
class UploadDocumentServiceTest {

    private static final String USER_ID = "1";
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String SERVICE_AUTH_TOKEN = "Bearer service token";

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private RequestData requestData;
    @Mock
    private DocumentUploadClientApi documentUploadClient;
    @Mock
    private CaseDocumentClientApi caseDocumentClientApi;

    @InjectMocks
    private SecureDocStoreService secureDocStoreService;

    private UploadDocumentService uploadDocumentService;

    @BeforeEach
    void setup() {
        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
        given(requestData.authorisation()).willReturn(AUTH_TOKEN);
        given(requestData.userId()).willReturn(USER_ID);

        uploadDocumentService = new UploadDocumentService(
            authTokenGenerator, documentUploadClient, requestData, secureDocStoreService, false
        );
    }

    @Test
    void shouldReturnFirstUploadedDocument() {
        UploadResponse request = DocumentManagementStoreLoader.successfulDocumentUploadResponse();
        given(documentUploadClient.upload(eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(USER_ID), any()))
            .willReturn(request);

        Document document = uploadDocumentService.uploadPDF(new byte[0], "file");

        Document received = oldToSecureDocument(request.getEmbedded().getDocuments().get(0));
        // Because old method returns old doc which needs to be converted, we can check equality per attribute, rather
        // than object equality
        Assertions.assertThat(document.classification).isEqualTo(received.classification);
        Assertions.assertThat(document.size).isEqualTo(received.size);
        Assertions.assertThat(document.mimeType).isEqualTo(received.mimeType);
        Assertions.assertThat(document.originalDocumentName).isEqualTo(received.originalDocumentName);
        Assertions.assertThat(document.createdOn).isEqualTo(received.createdOn);
        Assertions.assertThat(document.modifiedOn).isEqualTo(received.modifiedOn);
        Assertions.assertThat(document.createdBy).isEqualTo(received.createdBy);
        Assertions.assertThat(document.lastModifiedBy).isEqualTo(received.lastModifiedBy);
        Assertions.assertThat(document.ttl).isEqualTo(received.ttl);
        Assertions.assertThat(document.hashToken).isEqualTo(received.hashToken);
        Assertions.assertThat(document.metadata).isEqualTo(received.metadata);
    }

    @Test
    void shouldThrowExceptionIfServerResponseContainsNoDocuments() {
        given(documentUploadClient.upload(eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(USER_ID), any()))
            .willReturn(DocumentManagementStoreLoader.unsuccessfulDocumentUploadResponse());

        assertThatThrownBy(() -> uploadDocumentService.uploadPDF(new byte[0], "file"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Document upload failed due to empty result");
    }

    @Test
    void shouldRethrowExceptionIfServerCallThrownException() {
        given(documentUploadClient.upload(eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(USER_ID), any()))
            .willThrow(new RuntimeException("Something bad happened"));

        assertThatThrownBy(() -> uploadDocumentService.uploadPDF(new byte[0], "file"))
            .isInstanceOf(Exception.class)
            .hasMessage("Something bad happened");
    }
}
