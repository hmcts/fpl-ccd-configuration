package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    DocumentUploaderService.class
})
class DocumentUploaderServiceTest {
    private static final String AUTH_TOKEN = "token";
    private static final String USER_ID = "1";

    @MockBean
    private IdamClient idamClient;

    @Autowired
    private DocumentUploaderService documentUploaderService;

    @Test
    void shouldReturnUploadedDocumentUserInfoForUserWithHmctsRole() {
        when(idamClient.getUserDetails(eq(AUTH_TOKEN))).thenReturn(createUserDetailsWithHmctsRole());
        assertThat(documentUploaderService.getUploadedDocumentUserDetails(AUTH_TOKEN)).isEqualTo("HMCTS");
    }

    @Test
    void shouldReturnUploadedDocumentUserInfoForUserWithNonHmctsRole() {
        when(idamClient.getUserDetails(eq(AUTH_TOKEN))).thenReturn(createUserDetailsWithNonHmctsRole());
        assertThat(documentUploaderService.getUploadedDocumentUserDetails(AUTH_TOKEN)).isEqualTo("steve.hudson@gov.uk");
    }

    @Test
    void shouldReturnUploadedDocumentUserInfoForUserWithHmctsAndNonHmctsRole() {
        when(idamClient.getUserDetails(eq(AUTH_TOKEN))).thenReturn(createUserDetailsWithHmctsAndNonHmctsRole());
        assertThat(documentUploaderService.getUploadedDocumentUserDetails(AUTH_TOKEN)).isEqualTo("HMCTS");
    }

    private UserDetails createUserDetailsWithHmctsRole() {
        return UserDetails.builder()
            .id(USER_ID)
            .surname("Hudson")
            .forename("Steve")
            .email("steve.hudson@gov.uk")
            .roles(Arrays.asList("caseworker-publiclaw-courtadmin", "caseworker-publiclaw-judiciary"))
            .build();
    }

    private UserDetails createUserDetailsWithNonHmctsRole() {
        return UserDetails.builder()
            .id(USER_ID)
            .surname("Hudson")
            .forename("Steve")
            .email("steve.hudson@gov.uk")
            .roles(Arrays.asList("caseworker-publiclaw-solicitor", "caseworker-publiclaw-cafcass"))
            .build();
    }

    private UserDetails createUserDetailsWithHmctsAndNonHmctsRole() {
        return UserDetails.builder()
            .id(USER_ID)
            .surname("Hudson")
            .forename("Steve")
            .email("steve.hudson@gov.uk")
            .roles(Arrays.asList(
                "caseworker-publiclaw-solicitor",
                "caseworker-publiclaw-cafcass",
                "caseworker-publiclaw-superuser"))
            .build();
    }
}
