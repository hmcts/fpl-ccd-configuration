package uk.gov.hmcts.reform.fpl.events;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class CallbackEventTest {

    @Test
    void shouldCarryDataAndRequestContextInformation() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(RandomStringUtils.randomAlphanumeric(10))
            .build();

        CallbackEvent callbackEvent = new CallbackEvent(callbackRequest);

        assertThat(callbackEvent.getCallbackRequest()).isEqualTo(callbackRequest);
    }
}
