package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.exceptions.EmptyFileException;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static java.lang.String.join;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.CAFCASS;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ExtendWith(SpringExtension.class)
public class DocumentDownloadServiceTest {

    @Mock
    private DocumentDownloadClientApi documentDownloadClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamApi idamApi;

    @Mock
    private RequestData requestData;

    @Mock
    private ResponseEntity<Resource> resourceResponseEntity;

    @Mock
    private ByteArrayResource byteArrayResource;

    private DocumentDownloadService documentDownloadService;

    private Document document = document();

    @BeforeEach
    void setup() {
        String authToken = "token";
        String userId = "8a0a7c46-631c-4a55-9b81-4cc9fb9798f4";
        UserInfo userInfo = UserInfo.builder()
            .sub("cafcass@cafcass.com")
            .roles(CAFCASS.getRoles())
            .uid(userId)
            .build();

        given(authTokenGenerator.generate()).willReturn(authToken);
        given(idamApi.retrieveUserInfo(authToken)).willReturn(userInfo);
        given(requestData.authorisation()).willReturn(authToken);
        given(requestData.userId()).willReturn(userId);

        documentDownloadService = new DocumentDownloadService(authTokenGenerator,
            documentDownloadClient,
            idamApi,
            requestData);
    }

    @Test
    void shouldDownloadDocumentFromDocumentManagement() {
        Document document = document();
        byte[] expectedDocumentContents = "test".getBytes();

        ResponseEntity<Resource> expectedResponse = ResponseEntity.ok(new ByteArrayResource(expectedDocumentContents));
        given(resourceResponseEntity.getStatusCode())
            .willReturn(expectedResponse.getStatusCode());

        given(resourceResponseEntity.getBody())
            .willReturn(expectedResponse.getBody());

        given(documentDownloadClient.downloadBinary(anyString(), anyString(),
            eq(join(",", CAFCASS.getRoles())), anyString(), anyString()))
            .willReturn(resourceResponseEntity);

        byte[] documentContents = documentDownloadService.downloadDocument(document.links.binary.href);

        assertThat(documentContents).isNotEmpty();
        assertThat(documentContents).isEqualTo(expectedDocumentContents);

        verify(documentDownloadClient).downloadBinary(anyString(), anyString(),
            eq(join(",", CAFCASS.getRoles())), anyString(), anyString());
    }

    @Test
    void shouldThrowExceptionWhenDownloadBinaryReturnsNull() {
        ResponseEntity<Resource> responseEntity = ResponseEntity.notFound().build();
        given(resourceResponseEntity.getStatusCode()).willReturn(responseEntity.getStatusCode());
        given(documentDownloadClient.downloadBinary(anyString(), anyString(), anyString(), anyString(), anyString()))
            .willReturn(null);

        IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class,
            () -> documentDownloadService.downloadDocument(document.links.binary.href));
        assertThat(thrownException.getMessage()).contains("85d97996-22a5-40d7-882e-3a382c8ae1b4")
            .contains("/binary unsuccessful.");
    }

    @Test
    void shouldThrowExceptionWhenDocumentIsEmpty() {
        ResponseEntity<Resource> responseEntity = ResponseEntity.ok().body(byteArrayResource);
        given(byteArrayResource.getByteArray()).willReturn(null);
        given(documentDownloadClient.downloadBinary(anyString(), anyString(), anyString(), anyString(), anyString()))
            .willReturn(responseEntity);

        EmptyFileException exceptionThrown = assertThrows(EmptyFileException.class,
            () -> documentDownloadService.downloadDocument(document.links.binary.href));
        assertThat(exceptionThrown.getMessage()).isEqualTo("File cannot be empty");
    }

}
