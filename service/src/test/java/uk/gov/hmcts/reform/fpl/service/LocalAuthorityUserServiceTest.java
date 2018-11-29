package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.model.UserId;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityUserLookupConfiguration;
import uk.gov.hmcts.reform.fpl.exceptions.NoAssociatedUsersException;
import uk.gov.hmcts.reform.fpl.exceptions.UnknownLocalAuthorityCodeException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class LocalAuthorityUserServiceTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String SERVICE_AUTH_TOKEN = "Bearer service token";
    private static final String JURISDICTION = "PUBLICLAW";
    private static final String CASE_TYPE = "Shared_Storage_DRAFTType";
    private static final String USER_ID = "1";
    private static final String CASE_ID = "1";
    private static final String USER_TO_ADD = "5";
    private static final String LOCAL_AUTHORITY = "example";

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CaseAccessApi caseAccessApi;
    @Mock
    private LocalAuthorityUserLookupConfiguration localAuthorityUserLookupConfiguration;

    @InjectMocks
    private LocalAuthorityUserService localAuthorityUserService;

    @Test
    @SuppressWarnings({"LineLength"})
    void shouldCallGrantAccessToCaseWithExpectedParams() {
        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
        given(localAuthorityUserLookupConfiguration.getLookupTable()).willReturn(
            ImmutableMap.<String, List<String>>builder()
                .put(LOCAL_AUTHORITY, ImmutableList.<String>builder().add(USER_TO_ADD).build())
                .build()
        );

        localAuthorityUserService.grantUserAccess(AUTH_TOKEN, USER_ID, CASE_ID, LOCAL_AUTHORITY);

        verify(caseAccessApi, times(1)).grantAccessToCase(
            eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(USER_ID), eq(JURISDICTION), eq(CASE_TYPE), eq(CASE_ID), any(UserId.class));
    }

    @Test
    void shouldThrowCustomExceptionWhenLocalAuthorityCodeNotFound() throws IllegalArgumentException {
        given(localAuthorityUserLookupConfiguration.getLookupTable()).willReturn(
            ImmutableMap.<String, List<String>>builder()
                .put(LOCAL_AUTHORITY, ImmutableList.<String>builder().add(USER_TO_ADD).build())
                .build()
        );

        assertThatThrownBy(() ->
            localAuthorityUserService.grantUserAccess(AUTH_TOKEN, USER_ID, CASE_ID, "FT"))
            .isInstanceOf(UnknownLocalAuthorityCodeException.class)
            .hasMessage("The local authority: FT was not found");
    }

    @Test
    void shouldThrowCustomExceptionWhenValidLocalAuthorityHasNoUsers() throws IllegalArgumentException {
        given(localAuthorityUserLookupConfiguration.getLookupTable()).willReturn(
            ImmutableMap.<String, List<String>>builder()
                .put(LOCAL_AUTHORITY, ImmutableList.<String>builder().build())
                .build()
        );

        assertThatThrownBy(() ->
            localAuthorityUserService.grantUserAccess(AUTH_TOKEN, USER_ID, CASE_ID, LOCAL_AUTHORITY))
            .isInstanceOf(NoAssociatedUsersException.class)
            .hasMessage("No users found for the local authority: example");
    }

    @Test
    void userCreatingCaseDoesNotMakeCallToGrantAccess() {
        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
        given(localAuthorityUserLookupConfiguration.getLookupTable()).willReturn(
            ImmutableMap.<String, List<String>>builder()
                .put(LOCAL_AUTHORITY, ImmutableList.<String>builder().add("1").build())
                .build()
        );

        localAuthorityUserService.grantUserAccess(AUTH_TOKEN, USER_ID, CASE_ID, LOCAL_AUTHORITY);

        verify(caseAccessApi, never()).grantAccessToCase(
            any(), any(), any(), any(), any(), any(), any()
        );
    }
}
