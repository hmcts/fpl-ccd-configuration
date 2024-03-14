package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.CacheConfiguration;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.config.CacheConfiguration.SYS_USER_CACHE;
import static uk.gov.hmcts.reform.fpl.service.SystemUserService.SYS_USER_TOKEN_CACHE_KEY;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {SystemUserService.class, CacheConfiguration.class})
class SystemUserServiceTest {

    private static final String SYS_USER_NAME = "sys_name";
    private static final String SYS_USER_PASS = "sys_pass";

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private SystemUpdateUserConfiguration userConfig;

    @Autowired
    @Qualifier("localCacheManager")
    private CacheManager localCacheManager;

    @Autowired
    private SystemUserService underTest;

    @BeforeEach
    void init() {
        given(userConfig.getUserName()).willReturn(SYS_USER_NAME);
        given(userConfig.getPassword()).willReturn(SYS_USER_PASS);
    }

    @Test
    void shouldReturnSystemUserId() {
        String token = RandomStringUtils.randomAlphanumeric(10);

        UserInfo userInfo = UserInfo.builder()
            .uid(UUID.randomUUID().toString())
            .build();

        given(idamClient.getUserInfo(token)).willReturn(userInfo);

        String actualId = underTest.getUserId(token);

        assertThat(actualId).isEqualTo(userInfo.getUid());
    }

    @Test
    void shouldGetCachedTokenIfValid() {
        String token = RandomStringUtils.randomAlphanumeric(10);

        given(idamClient.getAccessToken(SYS_USER_NAME, SYS_USER_PASS)).willReturn(token);
        String retrieved = underTest.getSysUserToken();
        String retrievedSecond = underTest.getSysUserToken();

        // should get first time, then get cached version
        verify(idamClient, times(1)).getAccessToken(SYS_USER_NAME, SYS_USER_PASS);

        // ensure the token is the same all the time
        assertThat(retrieved).isEqualTo(token);
        assertThat(retrievedSecond).isEqualTo(token);
        assertThat(getCachedToken()).isEqualTo(token);
    }

    private String getCachedToken() {
        return localCacheManager.getCache(SYS_USER_CACHE).get(SYS_USER_TOKEN_CACHE_KEY, String.class);
    }

}
