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
class ComplyOnBehalfControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";

    //TODO: clean up of tests

    //TODO: respondent label
    @Test
    void aboutToStartCallbackShouldAddAllPartiesDirectionsIntoSeparateRoleCollections() throws Exception {
        Order sdo = Order.builder().directions(directionsForAllRoles()).build();

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.of("standardDirectionOrder", sdo))
                .build())
            .build();

        CaseData caseData = makeRequest(request, "about-to-start");

        assertThat(collectionsContainDirectionsForRoleAndAllParties(caseData));
    }

    @Test
    void aboutToSubmitShouldAddResponsesForParentsAndRespondents() throws Exception {
        UUID uuid = randomUUID();

        Order sdo = Order.builder()
            .directions(ImmutableList.of(Element.<Direction>builder()
                .id(uuid)
                .value(Direction.builder()
                    .directionType("example direction")
                    .assignee(PARENTS_AND_RESPONDENTS)
                    .build())
                .build()))
            .build();

        List<Element<Direction>> updatedRespondentDirection = ImmutableList.of(Element.<Direction>builder()
            .id(uuid)
            .value(Direction.builder()
                .responses(ImmutableList.of(Element.<DirectionResponse>builder()
                    .id(randomUUID())
                    .value(DirectionResponse.builder()
                        .complied("Yes")
                        .directionId(uuid)
                        .respondingOnBehalfOf("RESPONDENT_1")
                        .assignee(COURT)
                        .build())
                    .build()))
                .build())
            .build());

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.of(
                    "respondentDirectionsCustom", updatedRespondentDirection,
                    "standardDirectionOrder", sdo))
                .build())
            .build();

        CaseData caseData = makeRequest(request, "about-to-submit");

        assertThat(caseData.getStandardDirectionOrder().getDirections().get(0).getValue().getResponses()
            .containsAll(updatedRespondentDirection.get(0).getValue().getResponses()));
    }

    private List<Element<Direction>> directionsForAllRoles() {
        return Stream.of(DirectionAssignee.values())
            .map(directionAssignee -> Element.<Direction>builder()
                .id(randomUUID())
                .value(Direction.builder().assignee(directionAssignee).build())
                .build())
            .collect(toList());
    }

    private CaseData makeRequest(CallbackRequest request, String endpoint) throws Exception {
        MvcResult response = mockMvc
            .perform(post("/callback/comply-on-behalf/" + endpoint)
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
        return roleDirectionsContainExpectedDirections(caseData.getLocalAuthorityDirectionsCustom(), LOCAL_AUTHORITY)
            && roleDirectionsContainExpectedDirections(caseData.getCafcassDirectionsCustom(), CAFCASS)
            && roleDirectionsContainExpectedDirections(caseData.getCourtDirectionsCustom(), COURT)
            && roleDirectionsContainExpectedDirections(caseData.getOtherPartiesDirectionsCustom(), OTHERS)
            && roleDirectionsContainExpectedDirections(caseData.getRespondentDirectionsCustom(), PARENTS_AND_RESPONDENTS)
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
}
