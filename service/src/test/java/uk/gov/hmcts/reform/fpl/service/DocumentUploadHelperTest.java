package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    DocumentUploadHelper.class
})
class DocumentUploadHelperTest {
    private static final String AUTH_TOKEN = "token";
    private static final String USER_ID = "1";

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private RequestData requestData;

    @Autowired
    private DocumentUploadHelper documentUploadHelper;

    @BeforeEach
    void setUp() {
        when(requestData.authorisation()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void shouldReturnUploadedDocumentUserInfoForUserWithHmctsRole() {
        when(idamClient.getUserDetails(eq(AUTH_TOKEN))).thenReturn(createUserDetails(
            Arrays.asList("caseworker-publiclaw-courtadmin", "caseworker-publiclaw-judiciary")));
        assertThat(documentUploadHelper.getUploadedDocumentUserDetails()).isEqualTo("HMCTS");
    }

    @Test
    void shouldReturnUploadedDocumentUserInfoForUserWithNonHmctsRole() {
        when(idamClient.getUserDetails(eq(AUTH_TOKEN))).thenReturn(createUserDetails(
            Arrays.asList("caseworker-publiclaw-solicitor", "caseworker-publiclaw-cafcass")
        ));
        assertThat(documentUploadHelper.getUploadedDocumentUserDetails()).isEqualTo("steve.hudson@gov.uk");
    }

    @Test
    void shouldReturnUploadedDocumentUserInfoForUserWithHmctsAndNonHmctsRole() {
        when(idamClient.getUserDetails(eq(AUTH_TOKEN))).thenReturn(createUserDetails(
            Arrays.asList(
                "caseworker-publiclaw-solicitor",
                "caseworker-publiclaw-cafcass",
                "caseworker-publiclaw-superuser")
        ));
        assertThat(documentUploadHelper.getUploadedDocumentUserDetails()).isEqualTo("HMCTS");
    }

    private UserDetails createUserDetails(List<String> roles) {
        return UserDetails.builder()
            .id(USER_ID)
            .surname("Hudson")
            .forename("Steve")
            .email("steve.hudson@gov.uk")
            .roles(roles)
            .build();
    }
}
