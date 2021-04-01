package uk.gov.hmcts.reform.fpl.json.deserializer;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.HearingCancellationReason;

import static org.assertj.core.api.Assertions.assertThat;

class HearingCancellationReasonDeserializerTest extends DeserializerTest {

    HearingCancellationReasonDeserializerTest() {
        super(HearingCancellationReason.class, new HearingCancellationReasonDeserializer());
    }

    @Test
    void shouldDeserializeHearingCancellationReasonWithTypeSpecificReason() throws Exception {
        String jsonString = new JSONObject()
            .put("type", "Cafcass")
            .put("reason-Cafcass", "C1")
            .toString();

        HearingCancellationReason actual = mapper.readValue(jsonString, HearingCancellationReason.class);
        HearingCancellationReason expected = HearingCancellationReason.builder()
            .type("Cafcass")
            .reason("C1")
            .build();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldDeserializeHearingCancellationReasonWithGenericReason() throws Exception {
        String jsonString = new JSONObject()
            .put("reason", "C1")
            .toString();

        HearingCancellationReason actual = mapper.readValue(jsonString, HearingCancellationReason.class);
        HearingCancellationReason expected = HearingCancellationReason.builder()
            .reason("C1")
            .build();
        assertThat(actual).isEqualTo(expected);
    }
}


