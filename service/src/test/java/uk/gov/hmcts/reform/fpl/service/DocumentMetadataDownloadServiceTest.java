package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentMetadataDownloadClientApi;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static java.lang.String.join;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.CAFCASS;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ExtendWith(SpringExtension.class)
class DocumentMetadataDownloadServiceTest {
    private static final String AUTH_TOKEN = "token";
    private static final String SERVICE_AUTH_TOKEN = "service-token";
    private static final String USER_ID = "8a0a7c46-631c-4a55-9b81-4cc9fb9798f4";

    @Mock
    private DocumentMetadataDownloadClientApi documentMetadataDownloadClient;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private IdamClient idamClient;
    @Mock
    private RequestData requestData;
    @Mock
    private SecureDocStoreService secureDocStoreService;
    @Mock
    private FeatureToggleService featureToggleService;

    private DocumentMetadataDownloadService documentMetadataDownloadService;

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

        documentMetadataDownloadService = new DocumentMetadataDownloadService(authTokenGenerator,
                documentMetadataDownloadClient,
                idamClient,
                requestData,
                featureToggleService,
                secureDocStoreService);
    }

    @Test
    void shouldRetrieveDocumentFromDocumentManagement() {
        Document document = document();

        given(documentMetadataDownloadClient.getDocumentMetadata(anyString(), anyString(),
                eq(join(",", CAFCASS.getRoleNames())), anyString(), anyString()))
                .willReturn(document);

        DocumentReference documentReference = documentMetadataDownloadService.getDocumentMetadata(
                document.links.self.href
        );

        assertThat(documentReference.getSize()).isEqualTo(document.size);

        verify(documentMetadataDownloadClient).getDocumentMetadata(AUTH_TOKEN,
                SERVICE_AUTH_TOKEN,
                join(",", CAFCASS.getRoleNames()),
                USER_ID,
                "/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4");
    }

    @Test
    void shouldThrowExceptionWhenDocumentNotFound() {
        Document document = document();

        given(documentMetadataDownloadClient.getDocumentMetadata(anyString(), anyString(),
                eq(join(",", CAFCASS.getRoleNames())), anyString(), anyString()))
                .willReturn(null);


        assertThatThrownBy(() -> documentMetadataDownloadService.getDocumentMetadata(
                document.links.self.href
        )).isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Download of meta data unsuccessful for document ::http://localhost/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4");

        verify(documentMetadataDownloadClient).getDocumentMetadata(AUTH_TOKEN,
                SERVICE_AUTH_TOKEN,
                join(",", CAFCASS.getRoleNames()),
                USER_ID,
                "/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4");
    }
}
