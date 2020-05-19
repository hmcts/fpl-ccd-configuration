package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.CloseCaseReason.FINAL_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.CloseCaseReason.WITHDRAWN;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
class CloseCaseTest {

    @Autowired
    private ObjectMapper mapper;

    @Test
    void shouldSetShowFullReasonToYesWhenFullReasonIsPopulated() throws JsonProcessingException {
        String json = "{\"showFullReason\":\"YES\",\"date\":null,\"details\":null,\"fullReason\":\"FINAL_ORDER\","
            + "\"partialReason\":null}";

        CloseCase closeCase = mapper.readValue(json, CloseCase.class);
        CloseCase expected = CloseCase.builder().showFullReason(YES).reason(FINAL_ORDER).build();

        assertThat(closeCase).isEqualTo(expected);
    }

    @Test
    void shouldSetShowFullReasonToNoWhenPartialReasonIsPopulated() throws JsonProcessingException {
        String json = "{\"showFullReason\":\"NO\",\"date\":null,\"details\":null,\"fullReason\":null,"
            + "\"partialReason\":\"WITHDRAWN\"}";

        CloseCase closeCase = mapper.readValue(json, CloseCase.class);
        CloseCase expected = CloseCase.builder().showFullReason(NO).reason(WITHDRAWN).build();

        assertThat(closeCase).isEqualTo(expected);
    }

    @Test
    void shouldMapReasonToFullReasonWhenShowFullReasonIsYes() throws JsonProcessingException {
        CloseCase closeCase = CloseCase.builder().showFullReason(YES).reason(FINAL_ORDER).build();
        String expected = "{\"showFullReason\":\"YES\",\"date\":null,\"details\":null,\"fullReason\":\"FINAL_ORDER\","
            + "\"partialReason\":null}";

        String json = mapper.writeValueAsString(closeCase);

        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapReasonToPartialReasonWhenShowFullReasonIsNo() throws JsonProcessingException {
        CloseCase closeCase = CloseCase.builder().showFullReason(NO).reason(WITHDRAWN).build();
        String expected = "{\"showFullReason\":\"NO\",\"date\":null,\"details\":null,\"fullReason\":null,"
            + "\"partialReason\":\"WITHDRAWN\"}";

        String json = mapper.writeValueAsString(closeCase);

        JSONAssert.assertEquals(expected, json, true);
    }
}
