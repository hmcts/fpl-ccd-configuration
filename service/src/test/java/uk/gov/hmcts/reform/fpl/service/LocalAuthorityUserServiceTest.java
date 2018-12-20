package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import feign.RetryableException;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class LocalAuthorityUserServiceTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String SERVICE_AUTH_TOKEN = "Bearer service token";
    private static final String JURISDICTION = "PUBLICLAW";
    private static final String CASE_TYPE = "CARE_SUPERVISION_EPO";
    private static final String CASE_ID = "1";
    private static final String CREATOR_USER_ID = "1";
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
    void shouldThrowCustomExceptionWhenValidLocalAuthorityHasNoUsers() throws IllegalArgumentException {
        given(localAuthorityUserLookupConfiguration.getUserIds(LOCAL_AUTHORITY)).willReturn(
            ImmutableList.<String>builder().build()
        );

        assertThatThrownBy(() ->
            localAuthorityUserService.grantUserAccess(AUTH_TOKEN, CREATOR_USER_ID, CASE_ID, LOCAL_AUTHORITY))
            .isInstanceOf(NoAssociatedUsersException.class)
            .hasMessage("No users found for the local authority 'example'");
    }

    @Test
    void shouldNotMakeCallToGrantAccessEndpointWhenUserCreatingCaseIsOnlyUserWithinLocalAuthority() {
        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
        given(localAuthorityUserLookupConfiguration.getUserIds(LOCAL_AUTHORITY)).willReturn(
            ImmutableList.<String>builder()
                .add(CREATOR_USER_ID)
                .build()
        );

        localAuthorityUserService.grantUserAccess(AUTH_TOKEN, CREATOR_USER_ID, CASE_ID, LOCAL_AUTHORITY);

        verify(caseAccessApi, never()).grantAccessToCase(
            any(), any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    void shouldMakeCallToGrantAccessEndpointOnlyToGrantAccessToRemainingUsersWithinLocalAuthority() {
        String additionalUserId = "2";

        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
        given(localAuthorityUserLookupConfiguration.getUserIds(LOCAL_AUTHORITY)).willReturn(
            ImmutableList.<String>builder()
                .add(CREATOR_USER_ID, additionalUserId)
                .build()
        );

        localAuthorityUserService.grantUserAccess(AUTH_TOKEN, CREATOR_USER_ID, CASE_ID, LOCAL_AUTHORITY);

        verify(caseAccessApi, times(1)).grantAccessToCase(
            eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(CREATOR_USER_ID), eq(JURISDICTION),
            eq(CASE_TYPE), eq(CASE_ID), refEq(new UserId(additionalUserId)));
    }

    @Test
    void shouldNotThrowExceptionWhenCallToGrandAccessEndpointFailedForOneOfTheUsers() {
        String firstAdditionalUserId = "2";
        String secondAdditionalUserId = "3";

        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
        given(localAuthorityUserLookupConfiguration.getUserIds(LOCAL_AUTHORITY)).willReturn(
            ImmutableList.<String>builder()
                .add(CREATOR_USER_ID, firstAdditionalUserId, secondAdditionalUserId)
                .build()
        );
        willThrow(new RetryableException("Some error", null)).given(caseAccessApi).grantAccessToCase(
            eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(CREATOR_USER_ID), eq(JURISDICTION),
            eq(CASE_TYPE), eq(CASE_ID), refEq(new UserId(firstAdditionalUserId)));


        localAuthorityUserService.grantUserAccess(AUTH_TOKEN, CREATOR_USER_ID, CASE_ID, LOCAL_AUTHORITY);

        verify(caseAccessApi, times(1)).grantAccessToCase(
            eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(CREATOR_USER_ID), eq(JURISDICTION),
            eq(CASE_TYPE), eq(CASE_ID), refEq(new UserId(secondAdditionalUserId)));
    }
}
