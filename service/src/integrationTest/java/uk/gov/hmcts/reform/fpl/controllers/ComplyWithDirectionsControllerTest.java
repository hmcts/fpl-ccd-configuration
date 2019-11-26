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
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.DirectionResponse;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
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
class ComplyWithDirectionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";

    @Test
    void aboutToStartCallbackShouldAddAllPartiesDirectionsIntoSeparateRoleCollections() throws Exception {
        List<Direction> directions = directionsForAllRoles();
        Order sdo = Order.builder().directions(buildDirections(directions)).build();

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.of("standardDirectionOrder", sdo))
                .build())
            .build();

        CaseData caseData = makeRequest(request, "about-to-start");

        assertThat(collectionsContainDirectionsForRoleAndAllParties(caseData));
    }

    @SuppressWarnings("unchecked")
    @Test
    void aboutToSubmitCallbackShouldAddResponseToResponses() throws Exception {
        UUID uuid = randomUUID();

        List<Element<Direction>> directions = ImmutableList.of(Element.<Direction>builder()
            .id(uuid)
            .value(Direction.builder()
                .response(DirectionResponse.builder()
                    .complied("Yes")
                    .build())
                .build())
            .build());

        Order sdo = Order.builder().directions(ImmutableList.of(Element.<Direction>builder()
            .id(uuid)
            .value(Direction.builder()
                .directionType("example direction")
                .build())
            .build()))
            .build();

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(createCaseDataMap(directions)
                    .put("standardDirectionOrder", sdo)
                    .build())
                .build())
            .build();

        CaseData caseData = makeRequest(request, "about-to-submit");

        assertThat(caseData.getStandardDirectionOrder().getDirections().get(0).getValue().getResponses()).isNotEmpty();
    }

    private List<Direction> directionsForAllRoles() {
        return Stream.of(DirectionAssignee.values())
            .map(directionAssignee -> Direction.builder().assignee(directionAssignee).build())
            .collect(toList());
    }

    private List<Element<Direction>> buildDirections(List<Direction> directions) {
        return directions.stream().map(direction -> Element.<Direction>builder()
            .id(randomUUID())
            .value(direction)
            .build())
            .collect(toList());
    }

    private List<Element<Direction>> buildDirections(Direction direction) {
        return ImmutableList.of(Element.<Direction>builder()
            .id(randomUUID())
            .value(direction)
            .build());
    }

    private CaseData makeRequest(CallbackRequest request, String endpoint) throws Exception {
        MvcResult response = mockMvc
            .perform(post("/callback/comply-with-directions/" + endpoint)
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        AboutToStartOrSubmitCallbackResponse callbackResponse = mapper.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        return mapper.convertValue(callbackResponse.getData(), CaseData.class);
    }

    @SuppressWarnings("LineLength")
    private boolean collectionsContainDirectionsForRoleAndAllParties(CaseData caseData) {
        return roleDirectionsContainExpectedDirections(caseData.getLocalAuthorityDirections(), LOCAL_AUTHORITY)
            && roleDirectionsContainExpectedDirections(caseData.getCafcassDirections(), CAFCASS)
            && roleDirectionsContainExpectedDirections(caseData.getCourtDirectionsCustom(), COURT)
            && roleDirectionsContainExpectedDirections(caseData.getOtherPartiesDirections(), OTHERS)
            && roleDirectionsContainExpectedDirections(caseData.getRespondentDirections(), PARENTS_AND_RESPONDENTS)
            && roleDirectionsContainExpectedDirections(emptyList(), ALL_PARTIES);
    }

    private boolean roleDirectionsContainExpectedDirections(List<Element<Direction>> roleDirections,
                                                            DirectionAssignee assignee) {
        final Direction allPartiesDirection = Direction.builder().assignee(ALL_PARTIES).build();
        final Direction directions = Direction.builder().assignee(assignee).build();

        return roleDirections.stream()
            .map(Element::getValue)
            .anyMatch(x -> x.equals(allPartiesDirection) && x.equals(directions));
    }

    @SuppressWarnings("unchecked")
    private ImmutableMap.Builder createCaseDataMap(List<Element<Direction>> directions) {
        ImmutableMap.Builder builder = ImmutableMap.<String, List<Element<Direction>>>builder();

        return builder
            .put(LOCAL_AUTHORITY.getValue(), directions)
            .put(ALL_PARTIES.getValue(), buildDirections(Direction.builder().assignee(ALL_PARTIES).build()))
            .put(PARENTS_AND_RESPONDENTS.getValue(),
                buildDirections(Direction.builder().assignee(PARENTS_AND_RESPONDENTS).build()))
            .put(CAFCASS.getValue(), buildDirections(Direction.builder().assignee(CAFCASS).build()))
            .put(OTHERS.getValue(), buildDirections(Direction.builder().assignee(OTHERS).build()))
            .put(COURT.getValue(), buildDirections(Direction.builder().assignee(COURT).build()));
    }
}
