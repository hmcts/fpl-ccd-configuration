package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.fpl.CallbackRequestLogger;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogger;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogs;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class SystemUserServiceTest {

    private static final String SYS_USER_NAME = "sys_name";
    private static final String SYS_USER_PASS = "sys_pass";

    @Mock
    private IdamClient idamClient;

    @Mock
    private SystemUpdateUserConfiguration userConfig;

    @Mock
    private SystemUserCacheService cacheService;

    @TestLogs
    private final TestLogger logs = new TestLogger(SystemUserService.class);

    @InjectMocks
    private SystemUserService underTest;

    @BeforeEach
    void init() {
        when(userConfig.getUserName()).thenReturn(SYS_USER_NAME);
        when(userConfig.getPassword()).thenReturn(SYS_USER_PASS);
    }

    @Test
    void shouldReturnSystemUserId() {
        String token = RandomStringUtils.randomAlphanumeric(10);

        UserInfo userInfo = UserInfo.builder()
            .uid(UUID.randomUUID().toString())
            .build();

        when(idamClient.getUserInfo(token)).thenReturn(userInfo);

        String actualId = underTest.getUserId(token);

        assertThat(actualId).isEqualTo(userInfo.getUid());
    }

    @Test
    void shouldRequestNewTokenIfCacheDisabled() {
        String token = RandomStringUtils.randomAlphanumeric(10);
        when(idamClient.getAccessToken(SYS_USER_NAME, SYS_USER_PASS)).thenReturn(token);
        when(userConfig.isCacheTokenEnabled()).thenReturn(false);

        underTest.getSysUserToken();

        verifyNoInteractions(cacheService);
    }

    @Test
    void shouldUpdateNewTokenInCacheIfInvalid() {
        String token = RandomStringUtils.randomAlphanumeric(10);
        when(idamClient.getAccessToken(SYS_USER_NAME, SYS_USER_PASS)).thenReturn(token);

        when(userConfig.isCacheTokenEnabled()).thenReturn(true);
        when(cacheService.isCacheValid()).thenReturn(false);

        underTest.getSysUserToken();

        assertThat(logs.getInfos()).containsExactly("Cache invalid/missing, requesting new token");
        verify(cacheService).updateCache(token);
    }

    @Test
    void shouldGetCachedTokenIfValid() {
        String token = RandomStringUtils.randomAlphanumeric(10);

        when(userConfig.isCacheTokenEnabled()).thenReturn(true);
        when(cacheService.isCacheValid()).thenReturn(true);
        when(cacheService.getCachedToken()).thenReturn(token);

        String retrieved = underTest.getSysUserToken();

        assertThat(logs.getInfos()).containsExactly("Cache valid, using token");
        assertThat(retrieved).isEqualTo(token);
    }

}
