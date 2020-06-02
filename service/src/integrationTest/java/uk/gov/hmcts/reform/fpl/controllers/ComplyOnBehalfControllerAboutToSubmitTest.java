package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.DirectionResponse;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.ComplyOnBehalfEvent.COMPLY_ON_BEHALF_COURT;
import static uk.gov.hmcts.reform.fpl.enums.ComplyOnBehalfEvent.COMPLY_OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;

@ActiveProfiles("integration-test")
@WebMvcTest(ComplyOnBehalfController.class)
@OverrideAutoConfiguration(enabled = true)
class ComplyOnBehalfControllerAboutToSubmitTest extends AbstractControllerTest {
    private static final UUID DIRECTION_ID = randomUUID();

    @MockBean
    private UserDetailsService userDetailsService;

    private static final String AUTH_TOKEN = "Bearer token";

    ComplyOnBehalfControllerAboutToSubmitTest() {
        super("comply-on-behalf");
    }

    @Test
    void shouldAddResponsesOnBehalfOfPartyWhenCompliedWith() {
        CallbackRequest request = CallbackRequest.builder()
            .eventId(COMPLY_ON_BEHALF_COURT.toString())
            .caseDetails(CaseDetails.builder()
                .data(Map.of(
                    "standardDirectionOrder", orderWithAllPartiesDirection(),
                    "respondentDirectionsCustom", updatedDirection("RESPONDENT_1"),
                    "otherPartiesDirectionsCustom", updatedDirection("OTHER_1"),
                    "cafcassDirectionsCustom", updatedDirectionCafcass()))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(request);

        List<Element<DirectionResponse>> responses = mapper.convertValue(response.getData(), CaseData.class)
            .getStandardDirectionOrder().getDirections().get(0).getValue().getResponses();

        assertThat(ElementUtils.unwrapElements(responses))
            .containsOnly(
                expectedResponse("OTHER_1"),
                expectedResponse("RESPONDENT_1"),
                expectedResponse("CAFCASS"))
            .hasSize(3);
    }

    @Test
    void shouldAddResponsesOnBehalfOfWhenOtherEvent() {
        given(userDetailsService.getUserName()).willReturn("Emma Taylor");

        CallbackRequest request = CallbackRequest.builder()
            .eventId(COMPLY_OTHERS.toString())
            .caseDetails(CaseDetails.builder()
                .data(Map.of(
                    "standardDirectionOrder", orderWithAllPartiesDirection(),
                    "otherPartiesDirectionsCustom", updatedDirection("OTHER_1")))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(request);

        List<Element<DirectionResponse>> responses = mapper.convertValue(response.getData(), CaseData.class)
            .getStandardDirectionOrder().getDirections().get(0).getValue().getResponses();

        assertThat(ElementUtils.unwrapElements(responses))
            .containsOnly(DirectionResponse.builder()
                .complied("Yes")
                .responder("Emma Taylor")
                .directionId(DIRECTION_ID)
                .respondingOnBehalfOf("OTHER_1")
                .assignee(OTHERS)
                .build());
    }

    private List<Element<Direction>> updatedDirection(String onBehalfOf) {
        return List.of(Element.<Direction>builder()
            .id(DIRECTION_ID)
            .value(Direction.builder()
                .responses(List.of(ElementUtils.element(
                    DirectionResponse.builder()
                        .complied("Yes")
                        .respondingOnBehalfOf(onBehalfOf)
                        .build())))
                .build())
            .build());
    }

    private List<Element<Direction>> updatedDirectionCafcass() {
        return List.of(Element.<Direction>builder()
            .id(DIRECTION_ID)
            .value(Direction.builder()
                .response(DirectionResponse.builder()
                    .complied("Yes")
                    .respondingOnBehalfOf("CAFCASS")
                    .build())
                .build())
            .build());
    }

    private Order orderWithAllPartiesDirection() {
        return Order.builder()
            .directions(List.of(Element.<Direction>builder()
                .id(DIRECTION_ID)
                .value(Direction.builder()
                    .directionType("example direction")
                    .assignee(ALL_PARTIES)
                    .build())
                .build()))
            .build();
    }

    private DirectionResponse expectedResponse(String onBehalfOf) {
        return DirectionResponse.builder()
            .complied("Yes")
            .directionId(DIRECTION_ID)
            .respondingOnBehalfOf(onBehalfOf)
            .assignee(COURT)
            .build();
    }
}
