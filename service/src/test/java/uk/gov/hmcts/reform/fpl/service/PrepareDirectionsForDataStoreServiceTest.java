package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.DirectionResponse;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.ComplyOnBehalfEvent.COMPLY_ON_BEHALF_COURT;
import static uk.gov.hmcts.reform.fpl.enums.ComplyOnBehalfEvent.COMPLY_OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
class PrepareDirectionsForDataStoreServiceTest {

    @MockBean
    private UserDetailsService userDetailsService;

    private PrepareDirectionsForDataStoreService service;

    @BeforeEach
    void setUp() {
        service = new PrepareDirectionsForDataStoreService(userDetailsService, new CommonDirectionService());
    }

    @Test
    void persistHiddenDirectionValues_shouldAddValuesHiddenInCcdUiIncludingTextWhenReadOnlyIsYes() {
        UUID uuid = randomUUID();

        List<Element<Direction>> withHiddenValues = List.of(
            element(uuid, Direction.builder()
                .directionType("direction type")
                .directionText("hidden text")
                .readOnly("Yes")
                .directionRemovable("No")
                .build()));

        List<Element<Direction>> toAddValues = List.of(
            element(uuid, Direction.builder()
                .directionType("direction type")
                .build()));

        service.persistHiddenDirectionValues(withHiddenValues, toAddValues);

        assertThat(toAddValues).isEqualTo(withHiddenValues);
    }

    @Test
    void persistHiddenDirectionValues_shouldAddValuesHiddenInCcdUiExcludingTextWhenReadOnlyIsNo() {
        UUID uuid = randomUUID();

        List<Element<Direction>> withHiddenValues = List.of(
            element(uuid, Direction.builder()
                .directionType("direction type")
                .directionText("hidden text")
                .readOnly("No")
                .directionRemovable("No")
                .build()));

        List<Element<Direction>> toAddValues = List.of(
            element(uuid, Direction.builder()
                .directionType("direction type")
                .directionText("the expected text")
                .build()));

        service.persistHiddenDirectionValues(withHiddenValues, toAddValues);

        assertThat(toAddValues.get(0).getValue()).isEqualTo(Direction.builder()
            .directionType("direction type")
            .directionText("the expected text")
            .readOnly("No")
            .directionRemovable("No")
            .build());
    }

    @Nested
    class AddResponsesToDirections {
        private final UUID uuid = randomUUID();

        @Test
        void shouldAddNewResponsesWhenNonePreviouslyExisted() {
            DirectionResponse response = DirectionResponse.builder()
                .complied("Yes")
                .assignee(LOCAL_AUTHORITY)
                .directionId(uuid)
                .build();

            List<Element<Direction>> directionWithNoResponse = getDirectionsWithResponses(new ArrayList<>());

            service.addResponsesToDirections(ImmutableList.of(response), directionWithNoResponse);

            assertThat(getResponses(directionWithNoResponse)).isNotEmpty();
        }

        @Test
        void shouldNotAddResponseToDirectionWhenMatchingResponseAlreadyExist() {
            DirectionResponse response = DirectionResponse.builder()
                .complied("Yes")
                .assignee(LOCAL_AUTHORITY)
                .directionId(uuid)
                .build();

            List<Element<DirectionResponse>> responses = newArrayList(element(response));

            List<Element<Direction>> directionWithNoResponse = getDirectionsWithResponses(responses);

            service.addResponsesToDirections(ImmutableList.of(response), directionWithNoResponse);

            assertThat(getResponses(directionWithNoResponse)).hasSize(1);
        }

        @Test
        void shouldBeAbleToUpdateAnExistingResponseWhenAPartyChangesTheirResponse() {
            List<Element<DirectionResponse>> responses = Lists.newArrayList(
                element(DirectionResponse.builder()
                    .assignee(LOCAL_AUTHORITY)
                    .complied("No")
                    .directionId(uuid)
                    .build()));

            List<Element<Direction>> directionWithOldResponse = getDirectionsWithResponses(responses);

            DirectionResponse newResponse = DirectionResponse.builder()
                .assignee(LOCAL_AUTHORITY)
                .complied("Yes")
                .directionId(uuid)
                .build();

            service.addResponsesToDirections(List.of(newResponse), directionWithOldResponse);

            assertThat(getResponses(directionWithOldResponse)).hasSize(1);
            assertTrue(compliedFieldHasBeenUpdatedToYes(directionWithOldResponse));
        }

        @Test
        void shouldAddMultipleResponsesForTheSameDirectionWhenDifferentPartiesComply() {
            List<Element<Direction>> directionsWithNoResponse = getDirectionsWithResponses(new ArrayList<>());

            List<DirectionResponse> newResponses = List.of(DirectionResponse.builder()
                    .assignee(LOCAL_AUTHORITY)
                    .complied("Yes")
                    .directionId(uuid)
                    .build(),
                DirectionResponse.builder()
                    .assignee(CAFCASS)
                    .complied("Yes")
                    .directionId(uuid)
                    .build()
            );

            service.addResponsesToDirections(newResponses, directionsWithNoResponse);

            assertThat(getResponses(directionsWithNoResponse)).hasSize(2);
        }

        @Test
        void shouldBeAbleToUpdateAnExistingResponseWhenMultipleResponsesExist() {
            List<Element<DirectionResponse>> responses = Lists.newArrayList(
                element(
                    DirectionResponse.builder()
                        .assignee(LOCAL_AUTHORITY)
                        .complied("No")
                        .directionId(uuid)
                        .build()),
                element(
                    DirectionResponse.builder()
                        .assignee(CAFCASS)
                        .complied("No")
                        .directionId(uuid)
                        .build()));

            List<Element<Direction>> directionWithOldResponse = getDirectionsWithResponses(responses);

            DirectionResponse newResponse = DirectionResponse.builder()
                .assignee(LOCAL_AUTHORITY)
                .complied("Yes")
                .documentDetails("example details")
                .directionId(uuid)
                .build();

            service.addResponsesToDirections(List.of(newResponse), directionWithOldResponse);

            assertThat(getResponses(directionWithOldResponse)).hasSize(2);
            assertThat(getResponses(directionWithOldResponse)).extracting("value").contains(newResponse);
        }

        @Test
        void shouldUpdateCorrectResponseWhenMultipleResponses() {
            List<Element<DirectionResponse>> responses = Lists.newArrayList(
                element(DirectionResponse.builder()
                    .assignee(LOCAL_AUTHORITY)
                    .complied("No")
                    .directionId(uuid)
                    .build()),
                element(
                    DirectionResponse.builder()
                        .assignee(CAFCASS)
                        .complied("No")
                        .directionId(uuid)
                        .build()));

            List<Element<Direction>> directions = getDirectionsWithResponses(responses);

            List<DirectionResponse> newResponses = List.of(DirectionResponse.builder()
                    .assignee(LOCAL_AUTHORITY)
                    .complied("Yes")
                    .directionId(uuid)
                    .build(),
                DirectionResponse.builder()
                    .assignee(CAFCASS)
                    .complied("No")
                    .directionId(uuid)
                    .build()
            );

            service.addResponsesToDirections(newResponses, directions);

            assertThat(getResponses(directions)).hasSize(2);
            assertTrue(compliedFieldHasBeenUpdatedToYes(directions));
        }

        @Test
        void shouldAddAnotherResponseWhenDifferentRespondingOnBehalfOfValue() {
            List<Element<DirectionResponse>> responses = Lists.newArrayList(
                element(DirectionResponse.builder()
                    .assignee(COURT)
                    .respondingOnBehalfOf("CAFCASS")
                    .complied("No")
                    .directionId(uuid)
                    .build()));

            List<Element<Direction>> directions = getDirectionsWithResponses(responses);

            List<DirectionResponse> newResponses = List.of(DirectionResponse.builder()
                .assignee(COURT)
                .respondingOnBehalfOf("OTHER")
                .complied("No")
                .directionId(uuid)
                .build());

            service.addResponsesToDirections(newResponses, directions);

            assertThat(getResponses(directions)).hasSize(2);
        }

        @Test
        void shouldUpdateResponseWhenSameRespondingOnBehalfOfValue() {
            List<Element<DirectionResponse>> responses = newArrayList(
                element(DirectionResponse.builder()
                    .assignee(COURT)
                    .respondingOnBehalfOf("CAFCASS")
                    .complied("No")
                    .directionId(uuid)
                    .build()));

            List<Element<Direction>> directions = getDirectionsWithResponses(responses);

            List<DirectionResponse> newResponses = List.of(DirectionResponse.builder()
                .assignee(COURT)
                .respondingOnBehalfOf("CAFCASS")
                .complied("Yes")
                .directionId(uuid)
                .build());

            service.addResponsesToDirections(newResponses, directions);

            assertThat(getResponses(directions)).hasSize(1);
            assertTrue(compliedFieldHasBeenUpdatedToYes(directions));
        }

        private List<Element<Direction>> getDirectionsWithResponses(List<Element<DirectionResponse>> responses) {
            return List.of(element(uuid, Direction.builder()
                .responses(responses)
                .build()));
        }

        private List<Element<DirectionResponse>> getResponses(List<Element<Direction>> directionWithNoResponse) {
            return directionWithNoResponse.get(0).getValue().getResponses();
        }

        private boolean compliedFieldHasBeenUpdatedToYes(List<Element<Direction>> directions) {
            return getResponses(directions).get(0).getValue().getComplied().equals("Yes");
        }
    }

    @Nested
    class AddComplyOnBehalfResponsesToDirectionsInStandardDirectionOrder {
        private UUID directionId;
        private UUID responseId;

        @BeforeEach
        void initValues() {
            given(userDetailsService.getUserName("auth")).willReturn("Emma Taylor");

            directionId = randomUUID();
            responseId = randomUUID();
        }

        @Test
        void shouldAddCafcassResponseWhenValidResponseMadeByCourt() {
            Order sdo = orderWithCafcassDirection();
            List<Element<Direction>> directionWithResponse = directionWithCafcassResponse();

            CaseData caseData = CaseData.builder()
                .standardDirectionOrder(sdo)
                .cafcassDirectionsCustom(directionWithResponse)
                .build();

            DirectionResponse expectedResponse = DirectionResponse.builder()
                .directionId(directionId)
                .assignee(COURT)
                .respondingOnBehalfOf("CAFCASS")
                .complied("Yes")
                .build();

            service.addComplyOnBehalfResponsesToDirectionsInOrder(caseData, COMPLY_ON_BEHALF_COURT, "auth");

            assertThat(getResponsesSdo(caseData).get(0).getValue()).isEqualTo(expectedResponse);
        }

        @Test
        void shouldAddResponseForOtherPartiesWhenValidResponseMadeByCourt() {
            Direction.DirectionBuilder direction = Direction.builder().assignee(OTHERS);

            DirectionResponse.DirectionResponseBuilder response = DirectionResponse.builder()
                .complied("Yes")
                .respondingOnBehalfOf("OTHERS_1");

            CaseData caseData = prepareCaseData(direction, createResponses(response));

            List<Element<DirectionResponse>> expectedResponses = List.of(element(responseId,
                response.directionId(directionId)
                    .assignee(COURT)
                    .build()));

            service.addComplyOnBehalfResponsesToDirectionsInOrder(caseData, COMPLY_ON_BEHALF_COURT, "auth");

            assertThat(getResponsesSdo(caseData)).containsAll(expectedResponses);
        }

        @Test
        void shouldAddResponseForOtherPartiesWhenValidResponseMadeBySolicitor() {
            Direction.DirectionBuilder direction = Direction.builder().assignee(OTHERS);

            DirectionResponse.DirectionResponseBuilder response = DirectionResponse.builder()
                .complied("Yes")
                .respondingOnBehalfOf("OTHER_1");

            List<Element<DirectionResponse>> responses = createResponses(response);
            CaseData caseData = prepareCaseDataWithServedCmoAndResponseByOthers(direction, responses);

            List<Element<DirectionResponse>> expectedResponses = expectedResponse(OTHERS);

            service.addComplyOnBehalfResponsesToDirectionsInOrder(caseData, COMPLY_OTHERS, "auth");

            assertThat(getResponsesCmo(caseData)).containsAll(expectedResponses);
        }

        @Test
        void shouldAddResponseForRespondentWhenValidResponseMadeBySolicitor() {
            Direction.DirectionBuilder direction = Direction.builder().assignee(PARENTS_AND_RESPONDENTS);

            DirectionResponse.DirectionResponseBuilder response = DirectionResponse.builder()
                .complied("Yes")
                .respondingOnBehalfOf("OTHER_1");

            List<Element<DirectionResponse>> responses = createResponses(response);
            CaseData caseData = prepareCaseDataWithServedCmoAndRespondentResponse(direction, responses);

            List<Element<DirectionResponse>> expectedResponses = expectedResponse(PARENTS_AND_RESPONDENTS);

            service.addComplyOnBehalfResponsesToDirectionsInOrder(caseData, COMPLY_OTHERS, "auth");

            assertThat(getResponsesCmo(caseData)).containsAll(expectedResponses);
        }

        private List<Element<Direction>> directionWithCafcassResponse() {
            return List.of(element(directionId, Direction.builder()
                .response(DirectionResponse.builder()
                    .directionId(directionId)
                    .assignee(CAFCASS)
                    .complied("Yes")
                    .build())
                .build()));
        }

        private List<Element<DirectionResponse>> expectedResponse(DirectionAssignee others) {
            return List.of(element(responseId, DirectionResponse.builder()
                .directionId(directionId)
                .assignee(others)
                .responder("Emma Taylor")
                .complied("Yes")
                .respondingOnBehalfOf("OTHER_1")
                .build()));
        }

        private Order orderWithCafcassDirection() {
            return Order.builder()
                .directions(List.of(element(directionId, Direction.builder()
                    .directionType("example direction")
                    .assignee(CAFCASS)
                    .build())))
                .build();
        }


        private CaseData prepareCaseData(Direction.DirectionBuilder direction,
                                         List<Element<DirectionResponse>> responses) {
            return CaseData.builder()
                .standardDirectionOrder(Order.builder()
                    .directions(List.of(element(directionId, direction.build())))
                    .build())
                .otherPartiesDirectionsCustom(
                    List.of(element(directionId, direction.responses(responses).build())))
                .build();
        }

        private CaseData prepareCaseDataWithServedCmoAndResponseByOthers(Direction.DirectionBuilder direction,
                                                                         List<Element<DirectionResponse>> responses) {
            List<Element<CaseManagementOrder>> cmo = getCmo(direction);

            return CaseData.builder()
                .servedCaseManagementOrders(cmo)
                .otherPartiesDirectionsCustom(
                    List.of(element(directionId, direction.responses(responses).build())))
                .build();
        }

        private CaseData prepareCaseDataWithServedCmoAndRespondentResponse(Direction.DirectionBuilder direction,
                                                                           List<Element<DirectionResponse>> responses) {
            List<Element<CaseManagementOrder>> cmo = getCmo(direction);

            return CaseData.builder()
                .servedCaseManagementOrders(cmo)
                .respondentDirectionsCustom(
                    List.of(element(directionId, direction.responses(responses).build())))
                .build();
        }

        private List<Element<CaseManagementOrder>> getCmo(Direction.DirectionBuilder direction) {
            return Lists.newArrayList(element(
                CaseManagementOrder.builder()
                    .directions(List.of(element(directionId, direction.build())))
                    .build()));
        }

        private List<Element<DirectionResponse>> createResponses(DirectionResponse.DirectionResponseBuilder response) {
            return Lists.newArrayList(element(responseId, response.build()));
        }

        private List<Element<DirectionResponse>> getResponsesSdo(CaseData caseData) {
            return caseData.getStandardDirectionOrder().getDirections().get(0).getValue().getResponses();
        }

        private List<Element<DirectionResponse>> getResponsesCmo(CaseData caseData) {
            return caseData.getServedCaseManagementOrders().get(0).getValue()
                .getDirections().get(0).getValue().getResponses();
        }
    }

    @Nested
    class AddResponseElementsToDirection {
        UUID responseId = randomUUID();
        UUID directionId = randomUUID();

        @Test
        void shouldAddResponseElementWhenThereAreNoResponses() {
            List<Element<DirectionResponse>> responses = createDirectionResponses(responseId, directionId);

            List<Element<Direction>> directions = createDirections(directionId, new ArrayList<>());

            service.addResponseElementsToDirections(responses, directions);

            assertThat(directions.get(0).getValue().getResponses()).hasSize(1);
        }

        @Test
        void shouldAddResponseElementWhenThereAreResponsesWithDifferentResponseId() {
            List<Element<DirectionResponse>> responses = createDirectionResponses(responseId, directionId);

            List<Element<Direction>> directions =
                createDirections(directionId, createDirectionResponses(randomUUID(), directionId));

            service.addResponseElementsToDirections(responses, directions);

            assertThat(directions.get(0).getValue().getResponses()).hasSize(2);
        }

        @Test
        void shouldReplaceResponseElementWhenThereIsResponsesWithSameResponseId() {
            List<Element<DirectionResponse>> responses = createDirectionResponses(responseId, directionId);

            List<Element<Direction>> directions =
                createDirections(directionId, createDirectionResponses(responseId, directionId));

            service.addResponseElementsToDirections(responses, directions);

            assertThat(directions.get(0).getValue().getResponses()).hasSize(1);
        }

        @Test
        void shouldNotAddResponseElementWhenDifferentDirectionId() {
            List<Element<DirectionResponse>> responses = createDirectionResponses(responseId, randomUUID());

            List<Element<Direction>> directions = createDirections(directionId, new ArrayList<>());

            service.addResponseElementsToDirections(responses, directions);

            assertThat(directions.get(0).getValue().getResponses()).isEmpty();
        }

        private List<Element<Direction>> createDirections(UUID id, List<Element<DirectionResponse>> responses) {
            return Lists.newArrayList(element(id, Direction.builder()
                .responses(responses)
                .build()));
        }

        private List<Element<DirectionResponse>> createDirectionResponses(UUID responseId, UUID directionId) {
            return Lists.newArrayList(element(responseId, DirectionResponse.builder()
                .directionId(directionId)
                .build()));
        }
    }
}
