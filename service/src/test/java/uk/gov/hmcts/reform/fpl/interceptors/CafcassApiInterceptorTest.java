package uk.gov.hmcts.reform.fpl.interceptors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.exceptions.api.AuthorizationException;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.CAFCASS_SYSTEM_UPDATE;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.LOCAL_AUTHORITY;

@ExtendWith(MockitoExtension.class)
public class CafcassApiInterceptorTest {
    private final static String AUTH_TOKEN_TEST = "bearerToken";
    private final static UserInfo CAFCASS_SYSTEM_UPDATE_USER =
        UserInfo.builder().roles(List.of(CAFCASS_SYSTEM_UPDATE.getRoleName())).build();
    private final static UserInfo LOCAL_AUTHORITY_UPDATE_USER =
        UserInfo.builder().roles(List.of(LOCAL_AUTHORITY.getRoleName())).build();

    @Mock
    private IdamClient idamClient;
    @InjectMocks
    private CafcassApiInterceptor underTest;

    @Test
    public void shouldReturnTrueIfCafcassSystemUpdateUser() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(AUTH_TOKEN_TEST);
        when(idamClient.getUserInfo(AUTH_TOKEN_TEST)).thenReturn(CAFCASS_SYSTEM_UPDATE_USER);

        assertTrue(underTest.preHandle(request, null, null));
    }

    @Test
    public void shouldThrowAuthExceptionIfNotCafcassSystemUpdateUser() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(AUTH_TOKEN_TEST);
        when(idamClient.getUserInfo(AUTH_TOKEN_TEST)).thenReturn(LOCAL_AUTHORITY_UPDATE_USER);

        assertThrows(AuthorizationException.class,
            () -> underTest.preHandle(request, null, null));
    }

    @Test
    public void shouldThrowAuthExceptionWhenNoAuthorizationToken() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        assertThrows(AuthorizationException.class,
            () -> underTest.preHandle(request, null, null));
    }
}
