package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.CacheConfiguration;
import uk.gov.hmcts.reform.fpl.config.CafcassSystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.service.SystemUserService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.config.CacheConfiguration.CAFCASS_SYS_USER_CACHE;
import static uk.gov.hmcts.reform.fpl.config.CacheConfiguration.SYS_USER_CACHE;
import static uk.gov.hmcts.reform.fpl.service.SystemUserService.SYS_USER_TOKEN_CACHE_KEY;
import static uk.gov.hmcts.reform.fpl.service.cafcass.api.CafcassSystemUserService.CAFCASS_SYS_USER_TOKEN_CACHE_KEY;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {CafcassSystemUserService.class, CacheConfiguration.class, SystemUserService.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CafcassSystemUserServiceTest {

    private static final String CAFCASS_SYS_USER_NAME = "cafcass_sys_name";
    private static final String CAFCASSS_SYS_USER_PASS = "cafcass_sys_pass";

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private CafcassSystemUpdateUserConfiguration cafcassSysUserConfig;

    @MockBean
    private SystemUpdateUserConfiguration sysUserConfig;

    @Autowired
    private CacheManager localCacheManager;

    @Autowired
    private SystemUserService systemUserService;

    @Autowired
    private CafcassSystemUserService underTest;


    @BeforeEach
    void init() {
        given(cafcassSysUserConfig.getUserName()).willReturn(CAFCASS_SYS_USER_NAME);
        given(cafcassSysUserConfig.getPassword()).willReturn(CAFCASSS_SYS_USER_PASS);
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

        given(idamClient.getAccessToken(CAFCASS_SYS_USER_NAME, CAFCASSS_SYS_USER_PASS)).willReturn(token);
        String retrieved = underTest.getUserToken();
        String retrievedSecond = underTest.getUserToken();

        // should get first time, then get cached version
        verify(idamClient, times(1)).getAccessToken(CAFCASS_SYS_USER_NAME, CAFCASSS_SYS_USER_PASS);

        // ensure the token is the same all the time
        assertThat(retrieved).isEqualTo(token);
        assertThat(retrievedSecond).isEqualTo(token);
        assertThat(getCachedToken()).isEqualTo(token);
    }

    @Test
    void shouldNotInterfereAnyOtherCache() {
        String cafcassToken = RandomStringUtils.randomAlphanumeric(10);
        String sysToken = RandomStringUtils.randomAlphanumeric(10);

        given(sysUserConfig.getUserName()).willReturn("SYS_USER");
        given(sysUserConfig.getPassword()).willReturn("SYS_USER_PWD");
        given(idamClient.getAccessToken(CAFCASS_SYS_USER_NAME, CAFCASSS_SYS_USER_PASS)).willReturn(cafcassToken);
        given(idamClient.getAccessToken("SYS_USER", "SYS_USER_PWD")).willReturn(sysToken);

        underTest.getUserToken();
        systemUserService.getSysUserToken();

        for (int i = 0; i < 10; i++) {
            underTest.getUserToken();
            systemUserService.getSysUserToken();
            assertThat(localCacheManager.getCache(SYS_USER_CACHE).get(SYS_USER_TOKEN_CACHE_KEY, String.class))
                .isEqualTo(sysToken);
            assertThat(getCachedToken()).isEqualTo(cafcassToken);

            systemUserService.getSysUserToken();
            underTest.getUserToken();
            assertThat(localCacheManager.getCache(SYS_USER_CACHE).get(SYS_USER_TOKEN_CACHE_KEY, String.class))
                .isEqualTo(sysToken);
            assertThat(getCachedToken()).isEqualTo(cafcassToken);

            underTest.getUserToken();
            systemUserService.getSysUserToken();
            assertThat(getCachedToken()).isEqualTo(cafcassToken);
            assertThat(localCacheManager.getCache(SYS_USER_CACHE).get(SYS_USER_TOKEN_CACHE_KEY, String.class))
                .isEqualTo(sysToken);

            systemUserService.getSysUserToken();
            underTest.getUserToken();
            assertThat(getCachedToken()).isEqualTo(cafcassToken);
            assertThat(localCacheManager.getCache(SYS_USER_CACHE).get(SYS_USER_TOKEN_CACHE_KEY, String.class))
                .isEqualTo(sysToken);
        }
    }

    private String getCachedToken() {
        return localCacheManager.getCache(CAFCASS_SYS_USER_CACHE).get(CAFCASS_SYS_USER_TOKEN_CACHE_KEY, String.class);
    }

}
