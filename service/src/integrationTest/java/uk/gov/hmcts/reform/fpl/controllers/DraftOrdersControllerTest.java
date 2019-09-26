package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Disabled;
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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;

@ActiveProfiles("integration-test")
@WebMvcTest(DraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class DraftOrdersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";

    @Test
    void aboutToStartCallbackShouldSplitDirectionsIntoSeparateCollections() throws Exception {
        String TITLE = "example direction";

        List<Direction> directions = ImmutableList.of(
            Direction.builder().type(TITLE).assignee(ALL_PARTIES).build(),
            Direction.builder().type(TITLE).assignee(LOCAL_AUTHORITY).build(),
            Direction.builder().type(TITLE).assignee(PARENTS_AND_RESPONDENTS).build(),
            Direction.builder().type(TITLE).assignee(CAFCASS).build(),
            Direction.builder().type(TITLE).assignee(OTHERS).build(),
            Direction.builder().type(TITLE).assignee(COURT).build()
        );

        Order sdo = Order.builder().directions(buildDirections(directions)).build();

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.of("standardDirectionOrder", sdo))
                .build())
            .build();

        MvcResult response = mockMvc
            .perform(post("/callback/draft-SDO/about-to-start")
                .header("authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(extractDirections(caseData.getAllParties())).containsOnly(directions.get(0));
        assertThat(extractDirections(caseData.getLocalAuthorityDirections())).containsOnly(directions.get(1));
        assertThat(extractDirections(caseData.getParentsAndRespondentsDirections())).containsOnly(directions.get(2));
        assertThat(extractDirections(caseData.getCafcassDirections())).containsOnly(directions.get(3));
        assertThat(extractDirections(caseData.getOtherPartiesDirections())).containsOnly(directions.get(4));
        assertThat(extractDirections(caseData.getCourtDirections())).containsOnly(directions.get(5));
    }

    @Disabled
    @Test
    void midEventShouldGenerateDraftStandardDirectionDocument() throws Exception {
        List<Element<Direction>> directions = buildDirections(
            ImmutableList.of(Direction.builder().text("example").assignee(LOCAL_AUTHORITY).build())
        );


        //TODO: need to add directions for all parties. Currently throws null pointer
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.of(LOCAL_AUTHORITY.getValue(), directions))
                .build())
            .build();

        MvcResult response = mockMvc
            .perform(post("/callback/draft-SDO/mid-event")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(callbackResponse.getData().get("sdo")).isNotNull();
    }

    //TODO: aboutToSubmit test assert standardDirectionOrder is as expected.

    private List<Element<Direction>> buildDirections(List<Direction> directions) {
        return directions.stream().map(direction -> Element.<Direction>builder()
            .value(direction)
            .build())
            .collect(toList());
    }

    private List<Direction> extractDirections(List<Element<Direction>> directions) {
        return directions.stream().map(Element::getValue).collect(toList());
    }
}
