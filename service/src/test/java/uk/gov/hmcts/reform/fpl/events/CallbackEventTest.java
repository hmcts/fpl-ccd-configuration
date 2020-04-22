package uk.gov.hmcts.reform.fpl.events;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.request.RequestData;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class CallbackEventTest {

    private static final String AUTH_TOKEN = randomAlphanumeric(10);
    private static final String USER_ID = randomAlphanumeric(10);

    @Mock
    private RequestData requestData;

    @Test
    void shouldCarryDateAndRequestContextInformation() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(RandomStringUtils.randomAlphanumeric(10))
            .build();

        when(requestData.authorisation()).thenReturn(AUTH_TOKEN);
        when(requestData.userId()).thenReturn(USER_ID);

        CallbackEvent callbackEvent = new CallbackEvent(callbackRequest, requestData);

        assertThat(callbackEvent.getCallbackRequest()).isEqualTo(callbackRequest);
        assertThat(callbackEvent.getAuthorization()).isEqualTo(AUTH_TOKEN);
        assertThat(callbackEvent.getUserId()).isEqualTo(USER_ID);
    }
}
