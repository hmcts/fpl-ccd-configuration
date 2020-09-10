package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.Duration;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemUserServiceTest {

    private static final Duration ONE_MINUTE = Duration.parse("PT1M");
    private static final Duration FIVE_MILLIS = Duration.parse("PT0.005S");

    @Mock
    private IdamClient idamClient;

    private SystemUpdateUserConfiguration systemUser = new SystemUpdateUserConfiguration("name", "password");

    @Nested
    class AccessToken {

        @Test
        void shouldReturnCachedAccessToken() {
            Duration duration = ONE_MINUTE;
            String expectedAccessToken = randomAlphanumeric(10);

            SystemUserService systemUserService = new SystemUserService(systemUser, idamClient, duration, duration);

            when(idamClient.getAccessToken(systemUser.getUserName(), systemUser.getPassword()))
                .thenReturn(expectedAccessToken);

            String actualAccessToken1 = systemUserService.getAccessToken();
            String actualAccessToken2 = systemUserService.getAccessToken();

            assertThat(actualAccessToken1).isEqualTo(actualAccessToken2).isEqualTo(expectedAccessToken);

            verify(idamClient, times(1)).getAccessToken(systemUser.getUserName(), systemUser.getPassword());
        }

        @Test
        void shouldFetchAgainAccessTokenWhenCacheExpired() {
            Duration duration = FIVE_MILLIS;
            String expectedAccessToken1 = randomAlphanumeric(10);
            String expectedAccessToken2 = randomAlphanumeric(10);

            SystemUserService systemUserService = new SystemUserService(systemUser, idamClient, duration, duration);

            when(idamClient.getAccessToken(systemUser.getUserName(), systemUser.getPassword()))
                .thenReturn(expectedAccessToken1)
                .thenReturn(expectedAccessToken2);

            String actualAccessToken1 = systemUserService.getAccessToken();

            waitFor(duration);

            String actualAccessToken2 = systemUserService.getAccessToken();

            assertThat(actualAccessToken1).isEqualTo(expectedAccessToken1);
            assertThat(actualAccessToken2).isEqualTo(expectedAccessToken2);

            verify(idamClient, times(2)).getAccessToken(systemUser.getUserName(), systemUser.getPassword());
        }

        @Test
        void shouldRethrowsExceptions() {
            String expectedAccessToken = randomAlphanumeric(10);

            SystemUserService systemUserService = new SystemUserService(systemUser, idamClient, ONE_MINUTE, ONE_MINUTE);

            Exception expectedException = new RuntimeException("test");

            when(idamClient.getAccessToken(systemUser.getUserName(), systemUser.getPassword()))
                .thenThrow(expectedException)
                .thenReturn(expectedAccessToken);

            Exception actualException = assertThrows(Exception.class, systemUserService::getAccessToken);
            String actualAccessToken = systemUserService.getAccessToken();

            assertThat(actualException).isEqualTo(expectedException);
            assertThat(actualAccessToken).isEqualTo(expectedAccessToken);

            verify(idamClient, times(2)).getAccessToken(systemUser.getUserName(), systemUser.getPassword());
        }

        @Test
        void shouldThrowExceptionWhenCacheDurationIsGreaterThanExpectedTokenExpirationPeriod() {
            Exception actualException = assertThrows(Exception.class,
                () -> new SystemUserService(systemUser, idamClient, Duration.parse("PT8H"), ONE_MINUTE));

            assertThat(actualException)
                .hasMessage("System user token cache duration PT8H must be shorter than 8 hours");
        }
    }

    @Nested
    class UserId {

        @Test
        void shouldReturnCachedId() {
            Duration duration = ONE_MINUTE;
            String expectedAccessToken = randomAlphanumeric(10);
            UserInfo expectedUser = testUser();

            SystemUserService systemUserService = new SystemUserService(systemUser, idamClient, duration, duration);

            when(idamClient.getAccessToken(systemUser.getUserName(), systemUser.getPassword()))
                .thenReturn(expectedAccessToken);
            when(idamClient.getUserInfo(expectedAccessToken))
                .thenReturn(expectedUser);

            String actualUserId1 = systemUserService.getId();
            String actualUserId2 = systemUserService.getId();

            assertThat(actualUserId1).isEqualTo(actualUserId2).isEqualTo(expectedUser.getUid());

            verify(idamClient, times(1)).getAccessToken(systemUser.getUserName(), systemUser.getPassword());
            verify(idamClient, times(1)).getUserInfo(expectedAccessToken);
        }

        @Test
        void shouldFetchAgainUserIdWhenCacheExpired() {
            Duration duration = FIVE_MILLIS;
            String expectedAccessToken = randomAlphanumeric(10);
            UserInfo expectedUser1 = testUser();
            UserInfo expectedUser2 = testUser();

            SystemUserService systemUserService = new SystemUserService(systemUser, idamClient, ONE_MINUTE, duration);

            when(idamClient.getAccessToken(systemUser.getUserName(), systemUser.getPassword()))
                .thenReturn(expectedAccessToken);
            when(idamClient.getUserInfo(expectedAccessToken))
                .thenReturn(expectedUser1)
                .thenReturn(expectedUser2);

            String actualUserId1 = systemUserService.getId();

            waitFor(duration);

            String actualUserId2 = systemUserService.getId();

            assertThat(actualUserId1).isEqualTo(expectedUser1.getUid());
            assertThat(actualUserId2).isEqualTo(expectedUser2.getUid());

            verify(idamClient, times(1)).getAccessToken(systemUser.getUserName(), systemUser.getPassword());
            verify(idamClient, times(2)).getUserInfo(expectedAccessToken);
        }

        @Test
        void shouldRethrowsExceptions() {
            Duration duration = ONE_MINUTE;
            String expectedAccessToken = randomAlphanumeric(10);
            UserInfo userInfo = testUser();

            SystemUserService systemUserService = new SystemUserService(systemUser, idamClient, duration, duration);

            Exception expectedAccessTokenException = new RuntimeException("test");
            Exception expectedUserInfoException = new IllegalArgumentException("test");

            when(idamClient.getAccessToken(systemUser.getUserName(), systemUser.getPassword()))
                .thenThrow(expectedAccessTokenException)
                .thenReturn(expectedAccessToken);

            when(idamClient.getUserInfo(expectedAccessToken))
                .thenThrow(expectedUserInfoException)
                .thenReturn(userInfo);

            Exception actualException1 = assertThrows(Exception.class, systemUserService::getId);
            Exception actualException2 = assertThrows(Exception.class, systemUserService::getId);
            String actualUserId = systemUserService.getId();

            assertThat(actualException1).isEqualTo(expectedAccessTokenException);
            assertThat(actualException2).isEqualTo(expectedUserInfoException);
            assertThat(actualUserId).isEqualTo(userInfo.getUid());

            verify(idamClient, times(2)).getAccessToken(systemUser.getUserName(), systemUser.getPassword());
            verify(idamClient, times(2)).getUserInfo(expectedAccessToken);
        }
    }

    private static UserInfo testUser() {
        return UserInfo.builder()
            .name(randomAlphanumeric(10))
            .uid(randomAlphanumeric(10))
            .build();
    }

    private static void waitFor(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
