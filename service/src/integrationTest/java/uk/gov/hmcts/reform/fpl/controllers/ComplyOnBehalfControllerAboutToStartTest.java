package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.DirectionResponse;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.SERVED_CASE_MANAGEMENT_ORDERS;
import static uk.gov.hmcts.reform.fpl.enums.ComplyOnBehalfEvent.COMPLY_ON_BEHALF_COURT;
import static uk.gov.hmcts.reform.fpl.enums.ComplyOnBehalfEvent.COMPLY_OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;

@ActiveProfiles("integration-test")
@WebMvcTest(ComplyOnBehalfController.class)
@OverrideAutoConfiguration(enabled = true)
class ComplyOnBehalfControllerAboutToStartTest extends AbstractControllerTest {
    private static final UUID DIRECTION_ID = randomUUID();
    private static final UUID RESPONSE_ID = randomUUID();

    ComplyOnBehalfControllerAboutToStartTest() {
        super("comply-on-behalf");
    }

    @Test
    void shouldAddPartiesDirectionsIntoSeparateRoleCollectionsAndPopulateLabels() {
        CallbackRequest request = CallbackRequest.builder()
            .eventId(COMPLY_ON_BEHALF_COURT.toString())
            .caseDetails(CaseDetails.builder()
                .data(Map.of(
                    "standardDirectionOrder", Order.builder()
                        .directions(directionsForRespondentsCafcassOthersAndAllParties())
                        .build(),
                    "others", firstOther(),
                    "respondents1", respondents()
                ))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(request);
        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(actualResponses(caseData.getRespondentDirectionsCustom(), PARENTS_AND_RESPONDENTS))
            .isEqualTo(expectedResponses("RESPONDENT_1"))
            .hasSize(1);

        assertThat(getDirections(caseData.getCafcassDirectionsCustom(), CAFCASS).get(0).getValue().getResponse())
            .isEqualTo(expectedResponses("CAFCASS").get(0).getValue());

        assertThat(actualResponses(caseData.getOtherPartiesDirectionsCustom(), OTHERS))
            .isEqualTo(expectedResponses("OTHER_1"))
            .hasSize(1);

        assertThat(response.getData().get("respondents_label")).isEqualTo("Respondent 1 - John Doe\n");
        assertThat(response.getData().get("others_label")).isEqualTo("Person 1 - John Smith\n");
    }

    //Test to check persistence of responses issue.
    @Test
    void shouldAddAllPartiesDirectionWithPartyResponseToCorrectMap() {
        List<Element<Direction>> directions = getDirectionForRespondentsAllPartiesAndOthers();

        CallbackRequest request = CallbackRequest.builder()
            .eventId(COMPLY_ON_BEHALF_COURT.toString())
            .caseDetails(CaseDetails.builder()
                .data(Map.of(
                    "standardDirectionOrder", Order.builder().directions(directions).build(),
                    "others", firstOther(),
                    "respondents1", respondents()
                ))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(request);
        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(actualResponses(caseData.getRespondentDirectionsCustom(), ALL_PARTIES))
            .isEqualTo(responsesByCourtFor(PARENTS_AND_RESPONDENTS))
            .hasSize(1);

        assertThat(response.getData().get("respondents_label")).isEqualTo("Respondent 1 - John Doe\n");
        assertThat(response.getData().get("others_label")).isEqualTo("Person 1 - John Smith\n");
    }

    @Test
    void shouldAddDirectionWithPartyResponseToCorrectMapForComplyOthersEvent() {
        List<Element<Direction>> directions = directionsForRespondent();
        List<Element<CaseManagementOrder>> orders = List.of(Element.<CaseManagementOrder>builder()
            .value(CaseManagementOrder.builder().directions(directions).build())
            .build());

        CallbackRequest request = CallbackRequest.builder()
            .eventId(COMPLY_OTHERS.toString())
            .caseDetails(CaseDetails.builder()
                .data(Map.of(
                    "standardDirectionOrder", Order.builder().directions(directions).build(),
                    SERVED_CASE_MANAGEMENT_ORDERS.getKey(), orders,
                    "others", firstOther(),
                    "respondents1", respondents()
                ))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(request);
        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getRespondentDirectionsCustom().get(1).getValue().getResponses())
            .isEqualTo(responsesByRespondent())
            .hasSize(1);

        assertThat(response.getData().get("respondents_label")).isEqualTo("Respondent 1 - John Doe\n");
        assertThat(response.getData().get("others_label")).isEqualTo("Person 1 - John Smith\n");
    }

    private List<Element<Direction>> directionsForRespondentsCafcassOthersAndAllParties() {
        return Stream.of(new DirectionAssignee[]{PARENTS_AND_RESPONDENTS, CAFCASS, OTHERS, ALL_PARTIES})
            .map(directionAssignee -> Element.<Direction>builder()
                .id(DIRECTION_ID)
                .value(Direction.builder()
                    .assignee(directionAssignee)
                    .responses(responsesByCourtFor(directionAssignee))
                    .build())
                .build())
            .collect(toList());
    }

    private Others firstOther() {
        return Others.builder()
            .firstOther(Other.builder()
                .name("John Smith")
                .build())
            .build();
    }

    private List<Element<Respondent>> respondents() {
        return List.of(Element.<Respondent>builder()
            .value(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .build())
                .build())
            .build());
    }

    private List<Element<DirectionResponse>> actualResponses(List<Element<Direction>> directions,
                                                             DirectionAssignee assignee) {
        return getDirections(directions, assignee).get(0).getValue().getResponses();
    }

    private List<Element<Direction>> getDirections(List<Element<Direction>> directions, DirectionAssignee assignee) {
        return directions.stream().filter(x -> x.getValue().getAssignee() == assignee).collect(toList());
    }

    private List<Element<DirectionResponse>> responsesByCourtFor(DirectionAssignee assignee) {
        String respondingOnBehalfOf = "";

        if (assignee == PARENTS_AND_RESPONDENTS) {
            respondingOnBehalfOf = "RESPONDENT_1";
        }

        if (assignee == OTHERS) {
            respondingOnBehalfOf = "OTHER_1";
        }

        if (assignee == CAFCASS) {
            respondingOnBehalfOf = "CAFCASS";
        }

        if (assignee == ALL_PARTIES) {
            return emptyList();
        }

        return List.of(Element.<DirectionResponse>builder()
            .id(RESPONSE_ID)
            .value(DirectionResponse.builder()
                .complied("Yes")
                .directionId(DIRECTION_ID)
                .respondingOnBehalfOf(respondingOnBehalfOf)
                .assignee(COURT)
                .build())
            .build());
    }

    private List<Element<DirectionResponse>> expectedResponses(String onBehalfOf) {
        return List.of(Element.<DirectionResponse>builder()
            .id(RESPONSE_ID)
            .value(DirectionResponse.builder()
                .complied("Yes")
                .directionId(DIRECTION_ID)
                .respondingOnBehalfOf(onBehalfOf)
                .assignee(COURT)
                .build())
            .build());
    }

    private List<Element<Direction>> getDirectionForRespondentsAllPartiesAndOthers() {
        List<Element<Direction>> directions = new ArrayList<>();
        directions.add(Element.<Direction>builder()
            .id(DIRECTION_ID)
            .value(Direction.builder()
                .assignee(ALL_PARTIES)
                .responses(responsesByCourtFor(PARENTS_AND_RESPONDENTS))
                .build())
            .build());
        directions.add(Element.<Direction>builder()
            .id(randomUUID())
            .value(Direction.builder()
                .assignee(PARENTS_AND_RESPONDENTS)
                .build())
            .build());
        directions.add(Element.<Direction>builder()
            .id(randomUUID())
            .value(Direction.builder()
                .assignee(OTHERS)
                .build())
            .build());
        return directions;
    }

    private List<Element<Direction>> directionsForRespondent() {
        List<Element<Direction>> directions = new ArrayList<>();
        directions.add(Element.<Direction>builder()
            .id(DIRECTION_ID)
            .value(Direction.builder()
                .assignee(ALL_PARTIES)
                .responses(responsesByRespondent())
                .build())
            .build());
        directions.add(Element.<Direction>builder()
            .id(randomUUID())
            .value(Direction.builder()
                .assignee(PARENTS_AND_RESPONDENTS)
                .build())
            .build());
        return directions;
    }

    private List<Element<DirectionResponse>> responsesByRespondent() {
        return List.of(Element.<DirectionResponse>builder()
            .id(RESPONSE_ID)
            .value(DirectionResponse.builder()
                .complied("Yes")
                .directionId(DIRECTION_ID)
                .assignee(DirectionAssignee.PARENTS_AND_RESPONDENTS)
                .responder("Emma Taylor")
                .build())
            .build());
    }
}
