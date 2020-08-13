package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class UserDetailsServiceTest {

    @Mock
    private IdamClient idamClient;
    @Mock
    private RequestData requestData;
    @InjectMocks
    private UserDetailsService userDetailsService;

    @Test
    void shouldGetCurrentUserName() {
        String userToken = randomAlphanumeric(10);
        UserInfo userInfo = UserInfo.builder().name("John Smith").build();

        when(requestData.authorisation()).thenReturn(userToken);
        when(idamClient.getUserInfo(userToken)).thenReturn(userInfo);

        assertThat(userDetailsService.getUserName()).isEqualTo(userInfo.getName());
    }
}
