package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CMO;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseDeletionController.class)
@OverrideAutoConfiguration(enabled = true)
class ComplyWithOrdersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private static final String AUTH_TOKEN = "Bearer token";

    @Test
    void shouldReplaceExpectedDirectionWithModifiedCafcassDirections() throws Exception {
        Direction cmoDirection = Direction.builder()
            .title("direction 1")
            .build();

        Direction cafcassDirection = Direction.builder()
            .title("direction 1")
            .status("Yes")
            .build();

        UUID uuid = UUID.randomUUID();

        List<Element<Direction>> cmoDirections = new java.util.ArrayList<>(buildDirections(uuid, cmoDirection));
        cmoDirections.add(0, buildDirections(UUID.randomUUID(), cmoDirection).get(0));

        List<Element<Direction>> cafcassDirections = buildDirections(uuid, cafcassDirection);

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.of(
                    "cmoCollection", ImmutableList.of(Element.builder()
                        .value(CMO.builder()
                            .directions(cmoDirections)
                            .build())
                        .build()),
                    "cafcassDirections", cafcassDirections))
                .build())
            .build();

        MvcResult response = mockMvc
            .perform(post("/callback/comply-with-directions/about-to-submit")
                .header("authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getCmoCollection().get(0).getValue().getDirections()).containsAll(cafcassDirections);
    }

    private List<Element<Direction>> buildDirections(UUID uuid, Direction direction) {
        return ImmutableList.of(Element.<Direction>builder()
            .id(uuid)
            .value(direction)
            .build());
    }
}
