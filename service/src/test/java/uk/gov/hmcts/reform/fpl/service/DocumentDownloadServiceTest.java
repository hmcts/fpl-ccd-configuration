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
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static java.lang.String.join;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.CAFCASS;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ExtendWith(SpringExtension.class)
class DocumentDownloadServiceTest {

    private static final String AUTH_TOKEN = "token";
    private static final String SERVICE_AUTH_TOKEN = "service-token";
    private static final String USER_ID = "8a0a7c46-631c-4a55-9b81-4cc9fb9798f4";
    private static final String SYSTEM_USER_TOKEN = "system-user-token";
    private static final String SYSTEM_USER_ID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";

    @Mock
    private DocumentDownloadClientApi documentDownloadClient;

    @Mock
    private SecureDocStoreService secureDocStoreService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamClient idamClient;

    @Mock
    private RequestData requestData;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private ResponseEntity<Resource> resourceResponseEntity;

    @Mock
    private ByteArrayResource byteArrayResource;

    private DocumentDownloadService documentDownloadService;

    private final Document document = document();

    @BeforeEach
    void setup() {
        UserInfo userInfo = UserInfo.builder()
            .sub("cafcass@cafcass.com")
            .roles(CAFCASS.getRoleNames())
            .uid(USER_ID)
            .build();

        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
        given(idamClient.getUserInfo(AUTH_TOKEN)).willReturn(userInfo);
        given(requestData.authorisation()).willReturn(AUTH_TOKEN);
        given(requestData.userId()).willReturn(USER_ID);
        given(featureToggleService.isSecureDocstoreEnabled()).willReturn(false);
        given(systemUserService.getSysUserToken()).willReturn(SYSTEM_USER_TOKEN);
        given(systemUserService.getUserId(any())).willReturn(SYSTEM_USER_ID);

        documentDownloadService = new DocumentDownloadService(authTokenGenerator,
            documentDownloadClient,
            idamClient,
            requestData,
            secureDocStoreService,
            featureToggleService,
            systemUserService);
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
            eq(join(",", CAFCASS.getRoleNames())), anyString(), anyString()))
            .willReturn(resourceResponseEntity);

        byte[] documentContents = documentDownloadService.downloadDocument(document.links.binary.href);

        assertThat(documentContents).isEqualTo(expectedDocumentContents);

        verify(documentDownloadClient).downloadBinary(AUTH_TOKEN,
            SERVICE_AUTH_TOKEN,
            join(",", CAFCASS.getRoleNames()),
            USER_ID,
            "/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4/binary");
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

    @Test
    void shouldUseSystemUpdateUserIfNotInRequest() {
        given(requestData.authorisation()).willThrow(new IllegalStateException());

        Document document = document();
        byte[] expectedDocumentContents = "test".getBytes();

        ResponseEntity<Resource> expectedResponse = ResponseEntity.ok(new ByteArrayResource(expectedDocumentContents));
        given(resourceResponseEntity.getStatusCode())
            .willReturn(expectedResponse.getStatusCode());

        given(resourceResponseEntity.getBody())
            .willReturn(expectedResponse.getBody());

        given(documentDownloadClient.downloadBinary(anyString(), anyString(),
            anyString(), anyString(), anyString()))
            .willReturn(resourceResponseEntity);

        byte[] documentContents = documentDownloadService.downloadDocument(document.links.binary.href);

        assertThat(documentContents).isEqualTo(expectedDocumentContents);

        verify(documentDownloadClient).downloadBinary(SYSTEM_USER_TOKEN,
            SERVICE_AUTH_TOKEN,
            "caseworker-publiclaw-systemupdate",
            SYSTEM_USER_ID,
            "/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4/binary");

    }
}
