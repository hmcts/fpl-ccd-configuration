package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.successfulDocumentUploadResponse;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.unsuccessfulDocumentUploadResponse;

@ExtendWith(SpringExtension.class)
class UploadDocumentServiceTest {

    private static final String USER_ID = "1";
    private static final String USER_NAME = "SYS";
    private static final String PASSWORD = "SYSPASS";
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String SERVICE_AUTH_TOKEN = "Bearer service token";

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private DocumentUploadClientApi documentUploadClient;
    @Mock
    private RequestData requestData;
    @Mock
    private IdamClient idamClient;
    @Mock
    private SystemUpdateUserConfiguration userConfig;

    @InjectMocks
    private UploadDocumentService uploadDocumentService;

    @BeforeEach
    void setup() {
        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
        given(requestData.authorisation()).willReturn(AUTH_TOKEN);
        given(requestData.userId()).willReturn(USER_ID);
        given(userConfig.getUserName()).willReturn(USER_NAME);
        given(userConfig.getPassword()).willReturn(PASSWORD);
    }

    @Test
    void shouldReturnFirstUploadedDocument() {
        UploadResponse request = successfulDocumentUploadResponse();
        given(documentUploadClient.upload(eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(USER_ID), any()))
            .willReturn(request);

        Document document = uploadDocumentService.uploadPDF(new byte[0], "file");

        assertThat(document).isEqualTo(request.getEmbedded().getDocuments().get(0));
    }

    @Test
    void shouldThrowExceptionIfServerResponseContainsNoDocuments() {
        given(documentUploadClient.upload(eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(USER_ID), any()))
            .willReturn(unsuccessfulDocumentUploadResponse());

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

    @Nested
    class TestUploadedDocumentUserInfo {

        @BeforeEach
        void setup() {
            when(idamClient.getAccessToken(eq(USER_NAME), eq(PASSWORD))).thenReturn(AUTH_TOKEN);
        }

        @Test
        void shouldReturnUploadedDocumentUserInfoForUserWithHMCTSRole() {
            when(idamClient.getUserByUserId(eq(AUTH_TOKEN), eq(USER_ID))).thenReturn(createUserDetailsWithHMCTSRole());

            assertThat(uploadDocumentService.getUploadedDocumentUserInfo()).isEqualTo("HMCTS");
        }

        @Test
        void shouldReturnUploadedDocumentUserInfoForUserWithNonHMCTSRole() {
            when(idamClient.getUserByUserId(eq(AUTH_TOKEN), eq(USER_ID))).thenReturn(createUserDetailsWithNonHMCTSRole());

            assertThat(uploadDocumentService.getUploadedDocumentUserInfo()).isEqualTo("steve.hudson@gov.uk");
        }

        @Test
        void shouldReturnUploadedDocumentUserInfoForUserWithHMCTSAndNonHMCTSRole() {
            when(idamClient.getUserByUserId(eq(AUTH_TOKEN), eq(USER_ID))).thenReturn(createUserDetailsWithHMCTSAndNonHMCTSRole());

            assertThat(uploadDocumentService.getUploadedDocumentUserInfo()).isEqualTo("HMCTS");
        }

        private UserDetails createUserDetailsWithHMCTSRole() {
            return UserDetails.builder()
                .id(USER_ID)
                .surname("Hudson")
                .forename("Steve")
                .email("steve.hudson@gov.uk")
                .roles(Arrays.asList("caseworker-publiclaw-courtadmin", "caseworker-publiclaw-judiciary"))
                .build();
        }

        private UserDetails createUserDetailsWithNonHMCTSRole() {
            return UserDetails.builder()
                .id(USER_ID)
                .surname("Hudson")
                .forename("Steve")
                .email("steve.hudson@gov.uk")
                .roles(Arrays.asList("caseworker-publiclaw-solicitor", "caseworker-publiclaw-cafcass"))
                .build();
        }

        private UserDetails createUserDetailsWithHMCTSAndNonHMCTSRole() {
            return UserDetails.builder()
                .id(USER_ID)
                .surname("Hudson")
                .forename("Steve")
                .email("steve.hudson@gov.uk")
                .roles(Arrays.asList("caseworker-publiclaw-solicitor", "caseworker-publiclaw-cafcass", "caseworker-publiclaw-superuser"))
                .build();
        }
    }
}
