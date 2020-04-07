package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.DirectionResponse;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ComplyOnBehalfEvent.COMPLY_ON_BEHALF_COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;

class PrepareDirectionsForUsersServiceTest {
    private static final UUID DIRECTION_ID = randomUUID();
    private static final UUID RESPONSE_ID = randomUUID();

    private final PrepareDirectionsForUsersService service = new PrepareDirectionsForUsersService();

    @Nested
    class AddDirectionsToCaseDetails {

        @Test
        void shouldDoNothingWhenDirectionsDoNotNeedToBePopulatedForAllParties() {
            CaseDetails caseDetails = CaseDetails.builder().build();
            Map<DirectionAssignee, List<Element<Direction>>> directionsMap = new HashMap<>();
            directionsMap.put(ALL_PARTIES, buildDirections(ALL_PARTIES));

            service.addDirectionsToCaseDetails(caseDetails, directionsMap, COMPLY_ON_BEHALF_COURT);

            assertThat(caseDetails).isEqualTo(CaseDetails.builder().build());
            assertThat(directionsMap).isEqualTo(ImmutableMap.of(ALL_PARTIES, buildDirections(ALL_PARTIES)));
        }

        @Test
        void shouldDoNothingWhenDirectionsDoNotNeedToBePopulatedForCourt() {
            CaseDetails caseDetails = CaseDetails.builder().build();
            Map<DirectionAssignee, List<Element<Direction>>> directionsMap = new HashMap<>();
            directionsMap.put(COURT, buildDirections(COURT));
            directionsMap.put(ALL_PARTIES, buildDirections(ALL_PARTIES));

            service.addDirectionsToCaseDetails(caseDetails, directionsMap, COMPLY_ON_BEHALF_COURT);

            assertThat(caseDetails).isEqualTo(CaseDetails.builder().build());
            assertThat(directionsMap).isEqualTo(ImmutableMap.of(
                ALL_PARTIES, buildDirections(ALL_PARTIES),
                COURT, buildDirections(COURT)));
        }

        @ParameterizedTest
        @EnumSource(value = DirectionAssignee.class, names = {"PARENTS_AND_RESPONDENTS", "OTHERS"})
        void shouldPopulateDirectionsWhenListResponseDirections(DirectionAssignee assignee) {
            CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();
            Map<DirectionAssignee, List<Element<Direction>>> directionsMap = new HashMap<>();
            directionsMap.put(assignee, buildDirections(assignee));
            directionsMap.put(ALL_PARTIES, buildDirections(ALL_PARTIES));

            service.addDirectionsToCaseDetails(caseDetails, directionsMap, COMPLY_ON_BEHALF_COURT);

            List<Element<Direction>> expectedDirections = buildDirections(assignee);
            expectedDirections.addAll(buildDirections(ALL_PARTIES));

            assertThat(caseDetails).isEqualTo(CaseDetails.builder()
                .data(ImmutableMap.of(assignee.getValue().concat("Custom"), expectedDirections))
                .build());
        }

        // LOCAL_AUTHORITY would be added here if the functionality for complying on behalf of LA is implemented.
        @ParameterizedTest
        @EnumSource(value = DirectionAssignee.class, names = {"CAFCASS"})
        void shouldPopulateDirectionsWhenSingleResponseDirections(DirectionAssignee assignee) {
            CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();
            Map<DirectionAssignee, List<Element<Direction>>> directionsMap = new HashMap<>();
            directionsMap.put(assignee, buildDirections(assignee));
            directionsMap.put(ALL_PARTIES, buildDirections(ALL_PARTIES));

            service.addDirectionsToCaseDetails(caseDetails, directionsMap, COMPLY_ON_BEHALF_COURT);

            List<Element<Direction>> expectedDirections = buildDirections(assignee);
            expectedDirections.addAll(buildDirections(ALL_PARTIES));

            assertThat(caseDetails).isEqualTo(CaseDetails.builder()
                .data(ImmutableMap.of(assignee.getValue().concat("Custom"), expectedDirections))
                .build());
        }

        @Test
        void shouldPersistResponsesWhenAllPartiesDirectionHasBeenRespondedWith() {
            CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();

            Map<DirectionAssignee, List<Element<Direction>>> directionsMap = new HashMap<>();
            directionsMap.put(PARENTS_AND_RESPONDENTS, buildDirections(PARENTS_AND_RESPONDENTS));
            directionsMap.put(ALL_PARTIES, allPartyDirections());

            service.addDirectionsToCaseDetails(caseDetails, directionsMap, COMPLY_ON_BEHALF_COURT);

            List<Element<Direction>> expectedDirections = new ArrayList<>();
            expectedDirections.addAll(buildDirections(PARENTS_AND_RESPONDENTS));
            expectedDirections.addAll(allPartyDirections());

            assertThat(caseDetails).isEqualTo(CaseDetails.builder()
                .data(Map.of(PARENTS_AND_RESPONDENTS.getValue().concat("Custom"), expectedDirections))
                .build());
        }

        private List<Element<Direction>> allPartyDirections() {
            List<Element<DirectionResponse>> responses = responsesForRespondent();

            return buildDirections(ALL_PARTIES).stream()
                .map(element -> element.getValue().toBuilder()
                    .responses(responses)
                    .build())
                .map(element -> ElementUtils.element(DIRECTION_ID, element))
                .collect(toList());
        }

        private List<Element<DirectionResponse>> responsesForRespondent() {
            return Lists.newArrayList(ElementUtils.element(RESPONSE_ID, DirectionResponse.builder()
                .assignee(COURT)
                .respondingOnBehalfOf("RESPONDENT_1")
                .complied("Yes")
                .build()));
        }
    }

    @Nested
    class FilterResponsesNotCompliedOnBehalfOfByTheCourt {

        @Test
        void shouldFilterResponsesWhenResponseAssigneeIsNotCourt() {
            String onBehalfOf = "NOT_RELEVANT";

            List<Element<DirectionResponse>> responses = createResponses(LOCAL_AUTHORITY, "RESPONDENT_1");

            List<Element<Direction>> directions = createDirectionWithResponses(responses);

            service.filterResponsesNotCompliedOnBehalfOfByTheCourt(onBehalfOf, directions);

            assertThat(directions.get(0).getValue().getResponses()).isEmpty();
        }

        @Test
        void shouldNotErrorWhenResponsesInDirectionAreNull() {
            String onBehalfOf = "NOT_RELEVANT";

            List<Element<Direction>> directions = createDirectionWithResponses(null);

            service.filterResponsesNotCompliedOnBehalfOfByTheCourt(onBehalfOf, directions);

            assertThat(directions.get(0).getValue().getResponses()).isEmpty();
        }

        @Test
        void shouldFilterResponsesWhenEmptyOnBehalfOf() {
            String onBehalfOf = "RESPONDENT";
            List<Element<DirectionResponse>> responses = createResponses(COURT, null);

            List<Element<Direction>> directions = createDirectionWithResponses(responses);

            service.filterResponsesNotCompliedOnBehalfOfByTheCourt(onBehalfOf, directions);

            assertThat(directions.get(0).getValue().getResponses()).isEmpty();
        }

        @Test
        void shouldFilterResponsesWhenFilteringForDirectionsOnBehalfOfSomeoneElse() {
            String onBehalfOf = "OTHER";

            List<Element<DirectionResponse>> responses = createResponses(COURT, "RESPONDENT_1");

            List<Element<Direction>> directions = createDirectionWithResponses(responses);

            service.filterResponsesNotCompliedOnBehalfOfByTheCourt(onBehalfOf, directions);

            assertThat(directions.get(0).getValue().getResponses()).isEmpty();
        }

        @Test
        void shouldReturnResponsesWhenCorrectRespondingOnBehalfOf() {
            String onBehalfOf = "RESPONDENT";

            List<Element<DirectionResponse>> responses = createResponses(COURT, "RESPONDENT_1");

            List<Element<Direction>> directions = createDirectionWithResponses(responses);

            service.filterResponsesNotCompliedOnBehalfOfByTheCourt(onBehalfOf, directions);

            assertThat(directions.get(0).getValue().getResponses()).isEqualTo(responses);
        }

        private List<Element<DirectionResponse>> createResponses(DirectionAssignee assignee, String onBehalfOf) {
            return Lists.newArrayList(ElementUtils.element(DirectionResponse.builder()
                .assignee(assignee)
                .respondingOnBehalfOf(onBehalfOf)
                .build()));
        }

        private List<Element<Direction>> createDirectionWithResponses(List<Element<DirectionResponse>> responses) {
            return Lists.newArrayList(ElementUtils.element(Direction.builder()
                .responses(responses)
                .build()));
        }
    }

    @Nested
    class ExtractPartyResponse {
        private final UUID responseId = randomUUID();

        @Test
        void shouldExtractListResponsesWhenDirectionIdMatches() {
            List<Element<Direction>> directions = List.of(directionWithResponseFrom(LOCAL_AUTHORITY, responseId));

            List<Element<Direction>> expected = service.extractPartyResponse(LOCAL_AUTHORITY, directions);

            assertThat(expected.get(0).getValue().getResponse()).isNotNull();
        }

        @Test
        void shouldSetResponseToNullWhenDirectionIdDoesNotMatch() {
            List<Element<Direction>> directions =
                List.of(directionWithResponseForDifferentDirection(LOCAL_AUTHORITY, randomUUID(), randomUUID()));

            List<Element<Direction>> expected = service.extractPartyResponse(LOCAL_AUTHORITY, directions);

            assertThat(expected.get(0).getValue().getResponse()).isNull();
        }

        @Test
        void shouldSetResponseToNullWhenRoleDoesNotMatch() {
            List<Element<Direction>> directions = List.of(directionWithResponseFrom(CAFCASS, responseId));

            List<Element<Direction>> expected = service.extractPartyResponse(LOCAL_AUTHORITY, directions);

            assertThat(expected.get(0).getValue().getResponse()).isNull();
        }

        @Test
        void shouldSetResponseToNullWhenDirectionIdAndRoleDoesNotMatch() {
            List<Element<Direction>> directions = List.of(directionWithResponseFrom(CAFCASS, randomUUID()));

            List<Element<Direction>> expected = service.extractPartyResponse(LOCAL_AUTHORITY, directions);

            assertThat(expected.get(0).getValue().getResponse()).isNull();
        }

        @Test
        void shouldOnlyExtractPartyResponsesForGivenPartyWhenManyResponses() {
            List<Element<Direction>> directions = List.of(ElementUtils.element(
                responseId,
                Direction.builder()
                    .responses(List.of(
                        ElementUtils.element(DirectionResponse.builder()
                            .assignee(CAFCASS)
                            .directionId(responseId)
                            .build()),
                        ElementUtils.element(DirectionResponse.builder()
                            .assignee(LOCAL_AUTHORITY)
                            .directionId(responseId)
                            .build())))
                    .build()));

            List<Element<Direction>> expected = service.extractPartyResponse(LOCAL_AUTHORITY, directions);

            assertThat(expected.get(0).getValue().getResponse().getAssignee()).isEqualTo(LOCAL_AUTHORITY);
        }

        @Test
        void shouldPlaceResponsesWhenMultipleDirections() {
            List<Element<Direction>> directions = Lists.newArrayList(
                directionWithResponseFrom(LOCAL_AUTHORITY, responseId),
                directionWithResponseFrom(LOCAL_AUTHORITY, randomUUID()));

            List<Element<Direction>> expected = service.extractPartyResponse(LOCAL_AUTHORITY, directions);

            assertThat(expected.get(0).getValue().getResponse()).isNotNull();
            assertThat(expected.get(1).getValue().getResponse()).isNotNull();
            assertThat(expected).hasSize(2);
        }

        private Element<Direction> directionWithResponseFrom(DirectionAssignee assignee, UUID uuid) {
            return ElementUtils.element(uuid, Direction.builder()
                .responses(List.of(ElementUtils.element(
                    DirectionResponse.builder()
                        .assignee(assignee)
                        .directionId(uuid)
                        .build())))
                .build());
        }

        private Element<Direction> directionWithResponseForDifferentDirection(DirectionAssignee assignee,
                                                                              UUID directionId,
                                                                              UUID otherDirectionId) {
            return ElementUtils.element(directionId, Direction.builder()
                .responses(List.of(ElementUtils.element(
                    DirectionResponse.builder()
                        .assignee(assignee)
                        .directionId(otherDirectionId)
                        .build())))
                .build());
        }
    }

    @Nested
    class AddAssigneeDirectionKeyValuePairsToCaseData {

        @Test
        void shouldAddKeyValuePairWhenCaseDetailsIsEmpty() {
            CaseDetails caseDetails = CaseDetails.builder()
                .data(new HashMap<>())
                .build();

            service.addAssigneeDirectionKeyValuePairsToCaseData(
                LOCAL_AUTHORITY, buildDirections(LOCAL_AUTHORITY), caseDetails);

            assertThat(caseDetails.getData().get(LOCAL_AUTHORITY.getValue())).isEqualTo(expectedDirection());
        }

        @Test
        void shouldAddKeyValuePairWhenCaseDetailsAlreadyContainsThatKey() {
            Map<String, Object> data = new HashMap<>();
            data.put(LOCAL_AUTHORITY.getValue(), "some data");

            CaseDetails caseDetails = CaseDetails.builder()
                .data(data)
                .build();

            service.addAssigneeDirectionKeyValuePairsToCaseData(
                LOCAL_AUTHORITY, buildDirections(LOCAL_AUTHORITY), caseDetails);

            assertThat(caseDetails.getData().get(LOCAL_AUTHORITY.getValue())).isEqualTo(expectedDirection());
        }

        @Test
        void shouldAddKeyValuePairWhenCaseDetailsContainsOtherKeys() {
            Map<String, Object> data = new HashMap<>();
            data.put(CAFCASS.getValue(), "some data");

            CaseDetails caseDetails = CaseDetails.builder()
                .data(data)
                .build();

            service.addAssigneeDirectionKeyValuePairsToCaseData(
                LOCAL_AUTHORITY, buildDirections(LOCAL_AUTHORITY), caseDetails);

            assertThat(caseDetails.getData()).hasSize(2)
                .extracting(LOCAL_AUTHORITY.getValue())
                .isEqualTo(expectedDirection());
        }

        @Test
        void shouldAddKeyValuePairsWhenDirectionsToAddHaveDifferentAssignees() {
            CaseDetails caseDetails = CaseDetails.builder()
                .data(new HashMap<>())
                .build();

            List<Element<Direction>> directions = new ArrayList<>();
            directions.addAll(buildDirections(LOCAL_AUTHORITY));
            directions.addAll(buildDirections(ALL_PARTIES));

            service.addAssigneeDirectionKeyValuePairsToCaseData(LOCAL_AUTHORITY, directions, caseDetails);

            assertThat(caseDetails.getData()).hasSize(1)
                .extracting(LOCAL_AUTHORITY.getValue())
                .isEqualTo(directions);
        }

        @Test
        void shouldAddCustomKeyValuePairWhenDirectionsBelongToCourt() {
            CaseDetails caseDetails = CaseDetails.builder()
                .data(new HashMap<>())
                .build();

            List<Element<Direction>> directions = new ArrayList<>(buildDirections(COURT));

            service.addAssigneeDirectionKeyValuePairsToCaseData(COURT, directions, caseDetails);

            assertThat(caseDetails.getData()).hasSize(1)
                .extracting("courtDirectionsCustom")
                .isEqualTo(directions);
        }

        private List<Element<Direction>> expectedDirection() {
            return buildDirections(LOCAL_AUTHORITY);
        }
    }

    private List<Element<Direction>> buildDirections(DirectionAssignee assignee) {
        return Lists.newArrayList(ElementUtils.element(DIRECTION_ID, Direction.builder()
            .directionType("direction")
            .directionText("example direction text")
            .assignee(assignee)
            .build()));
    }
}
