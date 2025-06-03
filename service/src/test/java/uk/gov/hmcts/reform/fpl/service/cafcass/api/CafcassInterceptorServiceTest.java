package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiFeatureFlag;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.CAFCASS_SYSTEM_UPDATE;

@ExtendWith(MockitoExtension.class)
public class CafcassInterceptorServiceTest {
    private static final String AUTH_TOKEN_TEST = "bearerToken";
    private static final UserInfo CAFCASS_SYSTEM_UPDATE_USER =
        UserInfo.builder().roles(List.of(CAFCASS_SYSTEM_UPDATE.getRoleName())).build();

    @Mock
    private IdamClient idamClient;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private ObjectProvider<IdamClient> idamClientObjectProvider;
    @InjectMocks
    private CafcassInterceptorService underTest;

    @Test
    public void shouldReturnTrueIfToggledOn() {
        when(featureToggleService.getCafcassAPIFlag())
            .thenReturn(CafcassApiFeatureFlag.builder().enableApi(true).build());
        assertTrue(underTest.isCafcassApiToggledOn());
    }

    @Test
    public void shouldReturnFalseIfToggledOff() {
        when(featureToggleService.getCafcassAPIFlag())
            .thenReturn(CafcassApiFeatureFlag.builder().enableApi(false).build());
        assertFalse(underTest.isCafcassApiToggledOn());

        when(featureToggleService.getCafcassAPIFlag()).thenReturn(null);
        assertFalse(underTest.isCafcassApiToggledOn());
    }

    @Test
    public void shouldReturnIdamUserProfile() throws Exception {
        when(idamClientObjectProvider.getIfAvailable()).thenReturn(idamClient);
        when(idamClient.getUserInfo(AUTH_TOKEN_TEST)).thenReturn(CAFCASS_SYSTEM_UPDATE_USER);

        assertEquals(CAFCASS_SYSTEM_UPDATE_USER, underTest.getIdamUserInfo(AUTH_TOKEN_TEST));
    }
}
