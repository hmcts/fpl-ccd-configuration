package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.GATEKEEPER;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.HMCTS_ADMIN;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.HMCTS_SUPERUSER;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.JUDICIARY;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.LOCAL_AUTHORITY;

class UserServiceTest {

    private static final String USER_EMAIL = "user@email.com";
    private static final String USER_AUTHORISATION = "USER_AUTH";

    private final RequestData requestData = mock(RequestData.class);
    private final IdamClient client = mock(IdamClient.class);
    private final CaseAccessService accessService = mock(CaseAccessService.class);
    private final UserService underTest = new UserService(client, requestData, accessService);

    @Test
    void shouldReturnUserEmail() {
        when(requestData.authorisation()).thenReturn(USER_AUTHORISATION);
        when(client.getUserDetails(USER_AUTHORISATION)).thenReturn(UserDetails.builder()
            .email(USER_EMAIL)
            .build());

        String email = underTest.getUserEmail();

        assertThat(email).isEqualTo(USER_EMAIL);
    }

    @Test
    void shouldReturnUserName() {
        when(requestData.authorisation()).thenReturn(USER_AUTHORISATION);
        when(client.getUserDetails(USER_AUTHORISATION)).thenReturn(UserDetails.builder()
            .surname("Smith")
            .forename("John")
            .build());

        String name = underTest.getUserName();

        assertThat(name).isEqualTo("John Smith");
    }

    @Test
    void shouldCheckIfCurrentUserHasRole() {
        when(requestData.userRoles()).thenReturn(Set.of(GATEKEEPER.getRoleName(), CAFCASS.getRoleName()));

        assertThat(underTest.hasUserRole(JUDICIARY)).isFalse();
        assertThat(underTest.hasUserRole(HMCTS_ADMIN)).isFalse();
        assertThat(underTest.hasUserRole(HMCTS_SUPERUSER)).isFalse();
        assertThat(underTest.hasUserRole(LOCAL_AUTHORITY)).isFalse();

        assertThat(underTest.hasUserRole(GATEKEEPER)).isTrue();
        assertThat(underTest.hasUserRole(CAFCASS)).isTrue();
    }

    @Test
    void shouldReturnUserDetails() {
        final String userId = "1111-1111";
        final UserDetails expectedUserDetails = UserDetails.builder()
            .email(USER_EMAIL)
            .forename("Tom")
            .build();

        when(requestData.authorisation()).thenReturn(USER_AUTHORISATION);
        when(client.getUserByUserId(USER_AUTHORISATION, userId)).thenReturn(expectedUserDetails);

        final UserDetails actualUserDetails = underTest.getUserDetailsById(userId);

        assertThat(actualUserDetails).isEqualTo(expectedUserDetails);
    }


    @Nested
    class IsHmctsAdminUser {

        @Test
        void shouldReturnTrueIfUserHasOnlyAdminRole() {
            when(requestData.userRoles()).thenReturn(Set.of(HMCTS_ADMIN.getRoleName()));

            assertThat(underTest.isHmctsAdminUser()).isTrue();
        }

        @Test
        void shouldReturnTrueIfUserHasAdminRole() {
            when(requestData.userRoles()).thenReturn(Set.of(HMCTS_ADMIN.getRoleName(), JUDICIARY.getRoleName()));

            assertThat(underTest.isHmctsAdminUser()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "caseworker-publiclaw-solicitor",
            "caseworker-publiclaw-cafcass",
            "caseworker-publiclaw-gatekeeper",
            "caseworker-publiclaw-judiciary",
            "caseworker-publiclaw-superuser",
            "unknown-role"
        })
        void shouldReturnFalseIfUserDoesNotHaveAdminRole(String role) {
            when(requestData.userRoles()).thenReturn(Set.of(role));

            assertThat(underTest.isHmctsAdminUser()).isFalse();
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnFalseIfUserDoesNotHaveAnyRole(Set<String> roles) {
            when(requestData.userRoles()).thenReturn(roles);

            assertThat(underTest.isHmctsAdminUser()).isFalse();
        }
    }
}
