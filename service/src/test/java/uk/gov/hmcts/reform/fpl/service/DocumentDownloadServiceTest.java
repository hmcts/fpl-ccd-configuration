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
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.UUID;

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
    private final String token = "token";

    @Mock
    private DocumentDownloadClientApi documentDownloadClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamApi idamApi;

    @Mock
    private ResponseEntity<Resource> resourceResponseEntity;

    private DocumentDownloadService documentDownloadService;

    private String userId;

    private Document document;

    @BeforeEach
    void setup() throws Exception {
        document = document();

        given(authTokenGenerator.generate())
            .willReturn(token);

        userId = UUID.randomUUID().toString();

        UserInfo userInfo = UserInfo.builder()
            .sub("cafcass@cafcass.com")
            .roles(CAFCASS.getRoles())
            .uid(userId)
            .build();

        given(idamApi.retrieveUserInfo(token))
            .willReturn(userInfo);

        documentDownloadService = new DocumentDownloadService(authTokenGenerator, documentDownloadClient, idamApi);
    }

    @Test
    public void shouldDownloadDocumentFromDocumentManagement() throws Exception {
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

        byte[] documentContents = documentDownloadService.downloadDocument(token, userId, document.links.binary.href);

        assertThat(documentContents).isNotEmpty();
        assertThat(documentContents).isEqualTo(expectedDocumentContents);

        verify(documentDownloadClient).downloadBinary(anyString(), anyString(),
            eq(join(",", CAFCASS.getRoles())), anyString(), anyString());
    }

    @Test
    void shouldThrowExceptionWhenDownloadFromDocumentManagement() {
        ResponseEntity<Resource> expectedResponse = ResponseEntity.notFound().build();
        given(resourceResponseEntity.getStatusCode())
            .willReturn(expectedResponse.getStatusCode());

        given(documentDownloadClient.downloadBinary(anyString(), anyString(),
            eq(join(",", CAFCASS.getRoles())), anyString(), anyString()))
            .willReturn(null);

        assertThrows(IllegalArgumentException.class, () -> documentDownloadService.downloadDocument(
            token, userId, document.links.binary.href));
    }
}
