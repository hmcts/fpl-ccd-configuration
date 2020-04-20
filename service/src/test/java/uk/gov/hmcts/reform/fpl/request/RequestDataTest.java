package uk.gov.hmcts.reform.fpl.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class RequestDataTest {
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "example.gov.uk";

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private RequestData requestData;

    @Test
    public void shouldReturnAuthorisationHeaderFromRequest() {
        when(httpServletRequest.getHeader("authorization")).thenReturn(AUTH_TOKEN);
        assertThat(requestData.authorisation()).isEqualTo(AUTH_TOKEN);
    }

    @Test
    public void shouldReturnUserIdFromRequest() {
        when(httpServletRequest.getHeader("user-id")).thenReturn(USER_ID);
        assertThat(requestData.userId()).isEqualTo(USER_ID);
    }
}
