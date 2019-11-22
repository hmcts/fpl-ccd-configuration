package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.DirectionResponse;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.Display;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.EMPTY_LIST;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;

@ExtendWith(SpringExtension.class)
class DirectionHelperServiceTest {

    private final DirectionHelperService service = new DirectionHelperService();

    @Test
    void combineAllDirections_shouldAddRoleDirectionsIntoOneList() {
        CaseData caseData = populateCaseDataWithFixedDirections()
            .allPartiesCustom(buildCustomDirections())
            .localAuthorityDirectionsCustom(buildCustomDirections())
            .parentsAndRespondentsCustom(buildCustomDirections())
            .cafcassDirectionsCustom(buildCustomDirections())
            .otherPartiesDirectionsCustom(buildCustomDirections())
            .courtDirectionsCustom(buildCustomDirections())
            .build();

        List<Element<Direction>> directions = service.combineAllDirections(caseData);

        assertThat(directions).size().isEqualTo(12);
    }

    @Test
    void combineAllDirections_shouldAllowNullCustomDirectionValues() {
        CaseData caseData = populateCaseDataWithFixedDirections().build();

        List<Element<Direction>> directions = service.combineAllDirections(caseData);

        assertThat(directions).size().isEqualTo(6);
    }

    @Test
    void combineAllDirections_shouldAddCustomFlagOnlyToCustomDirection() {
        CaseData caseData = populateCaseDataWithFixedDirections()
            .courtDirectionsCustom(buildCustomDirections())
            .build();

        List<Element<Direction>> directions = service.combineAllDirections(caseData);

        List<Element<Direction>> directionWithCustomFlag = directions.stream()
            .filter(element -> element.getValue().getCustom() != null && element.getValue().getCustom().equals("Yes"))
            .collect(toList());

        assertThat(directionWithCustomFlag).hasSize(1);
    }

    @Test
    void combineAllDirections_shouldAssignCustomDirectionToCorrectAssignee() {
        CaseData caseData = populateCaseDataWithFixedDirections()
            .courtDirectionsCustom(buildCustomDirections())
            .build();

        List<Element<Direction>> directions = service.combineAllDirections(caseData);

        List<Element<Direction>> courtDirections = directions.stream()
            .filter(element -> element.getValue().getAssignee().equals(COURT))
            .collect(toList());

        assertThat(courtDirections).hasSize(2);
    }

    @Test
    void persistHiddenDirectionValues_shouldAddValuesHiddenInCcdUiIncludingTextWhenReadOnlyIsYes() {
        UUID uuid = UUID.randomUUID();

        List<Element<Direction>> withHiddenValues = ImmutableList.of(
            Element.<Direction>builder()
                .id(uuid)
                .value(Direction.builder()
                    .directionType("direction type")
                    .directionText("hidden text")
                    .readOnly("Yes")
                    .directionRemovable("No")
                    .build())
                .build());

        List<Element<Direction>> toAddValues = ImmutableList.of(
            Element.<Direction>builder()
                .id(uuid)
                .value(Direction.builder()
                    .directionType("direction type")
                    .build())
                .build());

        service.persistHiddenDirectionValues(withHiddenValues, toAddValues);

        assertThat(toAddValues).isEqualTo(withHiddenValues);
    }

    @Test
    void persistHiddenDirectionValues_shouldAddValuesHiddenInCcdUiExcludingTextWhenReadOnlyIsNo() {
        UUID uuid = UUID.randomUUID();

        List<Element<Direction>> withHiddenValues = ImmutableList.of(
            Element.<Direction>builder()
                .id(uuid)
                .value(Direction.builder()
                    .directionType("direction type")
                    .directionText("hidden text")
                    .readOnly("No")
                    .directionRemovable("No")
                    .build())
                .build());

        List<Element<Direction>> toAddValues = ImmutableList.of(
            Element.<Direction>builder()
                .id(uuid)
                .value(Direction.builder()
                    .directionType("direction type")
                    .directionText("the expected text")
                    .build())
                .build());

        service.persistHiddenDirectionValues(withHiddenValues, toAddValues);

        assertThat(toAddValues.get(0).getValue()).isEqualTo(Direction.builder()
            .directionType("direction type")
            .directionText("the expected text")
            .readOnly("No")
            .directionRemovable("No")
            .build());
    }

    @Test
    void numberDirections_shouldNumberDirectionsStartingAtTwo() {
        CaseData caseData = populateCaseDataWithFixedDirections().build();

        List<Element<Direction>> directions = service.combineAllDirections(caseData);

        List<String> numberedDirectionTypes = service.numberDirections(directions).stream()
            .map(direction -> direction.getValue().getDirectionType())
            .collect(toList());

        List<String> expectedDirectionsTypes = IntStream.range(0, numberedDirectionTypes.size())
            .mapToObj(x -> (x + 2) + ". direction")
            .collect(toList());

        assertThat(numberedDirectionTypes).isEqualTo(expectedDirectionsTypes);
    }

    @Test
    void constructDirectionForCCD_shouldConstructDirectionFromConfigurationAsExpectedWhenCompleteByDateIsRealDate() {
        LocalDateTime today = LocalDateTime.now();

        DirectionConfiguration directionConfig = DirectionConfiguration.builder()
            .assignee(LOCAL_AUTHORITY)
            .title("direction title")
            .text("direction text")
            .display(Display.builder()
                .due(Display.Due.BY)
                .templateDateFormat("h:mma, d MMMM yyyy")
                .directionRemovable(false)
                .showDateOnly(false)
                .build())
            .build();

        Element<Direction> actualDirection = service.constructDirectionForCCD(directionConfig, today);

        assertThat(actualDirection.getValue()).isEqualTo(Direction.builder()
            .directionType("direction title")
            .directionText("direction text")
            .readOnly("No")
            .directionRemovable("No")
            .dateToBeCompletedBy(today)
            .assignee(LOCAL_AUTHORITY)
            .build());
    }

    @Test
    void constructDirectionForCCD_shouldConstructDirectionFromConfigurationAsExpectedWhenCompleteByDateIsNull() {
        DirectionConfiguration directionConfig = DirectionConfiguration.builder()
            .assignee(LOCAL_AUTHORITY)
            .title("direction title")
            .text("direction text")
            .display(Display.builder()
                .due(Display.Due.BY)
                .templateDateFormat("h:mma, d MMMM yyyy")
                .directionRemovable(false)
                .showDateOnly(false)
                .build())
            .build();

        Element<Direction> actualDirection =
            service.constructDirectionForCCD(directionConfig, null);

        assertThat(actualDirection.getValue()).isEqualTo(Direction.builder()
            .directionType("direction title")
            .directionText("direction text")
            .readOnly("No")
            .directionRemovable("No")
            .dateToBeCompletedBy(null)
            .assignee(LOCAL_AUTHORITY)
            .build());
    }

    @Nested
    class AddResponsesToDirections {
        private final UUID uuid = UUID.randomUUID();

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

            List<Element<DirectionResponse>> responses = new ArrayList<>();
            responses.add(Element.<DirectionResponse>builder()
                .value(response)
                .build());

            List<Element<Direction>> directionWithNoResponse = getDirectionsWithResponses(responses);

            service.addResponsesToDirections(ImmutableList.of(response), directionWithNoResponse);

            assertThat(getResponses(directionWithNoResponse)).hasSize(1);
        }

        @Test
        void shouldBeAbleToUpdateAnExistingResponseWhenAPartyChangesTheirResponse() {
            List<Element<DirectionResponse>> responses = new ArrayList<>();
            responses.add(Element.<DirectionResponse>builder()
                .value(DirectionResponse.builder()
                    .assignee(LOCAL_AUTHORITY)
                    .complied("No")
                    .directionId(uuid)
                    .build())
                .build());

            List<Element<Direction>> directionWithOldResponse = getDirectionsWithResponses(responses);

            DirectionResponse newResponse = DirectionResponse.builder()
                .assignee(LOCAL_AUTHORITY)
                .complied("Yes")
                .directionId(uuid)
                .build();

            service.addResponsesToDirections(ImmutableList.of(newResponse), directionWithOldResponse);

            assertThat(getResponses(directionWithOldResponse)).hasSize(1);
            assertThat(getResponses(directionWithOldResponse).get(0).getValue().getComplied()).isEqualTo("Yes");
        }

        @Test
        void shouldAddMultipleResponsesForTheSameDirectionWhenDifferentPartiesComply() {
            List<Element<Direction>> directionsWithNoResponse = getDirectionsWithResponses(new ArrayList<>());

            List<DirectionResponse> newResponses = ImmutableList.of(DirectionResponse.builder()
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
            List<Element<DirectionResponse>> responses = new ArrayList<>();
            responses.add(Element.<DirectionResponse>builder()
                .value(DirectionResponse.builder()
                    .assignee(LOCAL_AUTHORITY)
                    .complied("No")
                    .directionId(uuid)
                    .build())
                .build());

            responses.add(Element.<DirectionResponse>builder()
                .value(DirectionResponse.builder()
                    .assignee(CAFCASS)
                    .complied("No")
                    .directionId(uuid)
                    .build())
                .build());

            List<Element<Direction>> directionWithOldResponse = getDirectionsWithResponses(responses);

            DirectionResponse newResponse = DirectionResponse.builder()
                .assignee(LOCAL_AUTHORITY)
                .complied("Yes")
                .documentDetails("example details")
                .directionId(uuid)
                .build();

            service.addResponsesToDirections(ImmutableList.of(newResponse), directionWithOldResponse);

            assertThat(getResponses(directionWithOldResponse)).hasSize(2);
            assertThat(getResponses(directionWithOldResponse)).extracting("value").contains(newResponse);
        }

        @Test
        void shouldUpdateCorrectResponseWhenMultipleResponses() {
            List<Element<DirectionResponse>> responses = new ArrayList<>();
            responses.add(Element.<DirectionResponse>builder()
                .value(DirectionResponse.builder()
                    .assignee(LOCAL_AUTHORITY)
                    .complied("No")
                    .directionId(uuid)
                    .build())
                .build());

            responses.add(Element.<DirectionResponse>builder()
                .value(DirectionResponse.builder()
                    .assignee(CAFCASS)
                    .complied("No")
                    .directionId(uuid)
                    .build())
                .build());

            List<Element<Direction>> directions = getDirectionsWithResponses(responses);

            List<DirectionResponse> newResponses = ImmutableList.of(DirectionResponse.builder()
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
            assertThat(directions.get(0).getValue().getResponses().get(0).getValue().getComplied()).isEqualTo("Yes");
        }

        private List<Element<Direction>> getDirectionsWithResponses(List<Element<DirectionResponse>> responses) {
            return ImmutableList.of(
                Element.<Direction>builder()
                    .id(uuid)
                    .value(Direction.builder()
                        .responses(responses)
                        .build())
                    .build());
        }

        private List<Element<DirectionResponse>> getResponses(List<Element<Direction>> directionWithNoResponse) {
            return directionWithNoResponse.get(0).getValue().getResponses();
        }
    }

    @Nested
    class ExtractPartyResponse {
        private final UUID uuid = UUID.randomUUID();

        @Test
        void shouldExtractListResponsesWhenDirectionIdMatches() {
            List<Element<Direction>> directions = ImmutableList.of(Element.<Direction>builder()
                .id(uuid)
                .value(Direction.builder()
                    .responses(ImmutableList.of(Element.<DirectionResponse>builder()
                        .value(DirectionResponse.builder()
                            .assignee(LOCAL_AUTHORITY)
                            .directionId(uuid)
                            .build())
                        .build()))
                    .build())
                .build());

            List<Element<Direction>> expected = service.extractPartyResponse(LOCAL_AUTHORITY.getValue(), directions);

            assertThat(expected.get(0).getValue().getResponse()).isNotNull();
        }

        @Test
        void shouldSetResponseToNullWhenDirectionIdDoesNotMatch() {
            List<Element<Direction>> directions = ImmutableList.of(Element.<Direction>builder()
                .id(uuid)
                .value(Direction.builder()
                    .responses(ImmutableList.of(Element.<DirectionResponse>builder()
                        .value(DirectionResponse.builder()
                            .assignee(LOCAL_AUTHORITY)
                            .directionId(UUID.randomUUID())
                            .build())
                        .build()))
                    .build())
                .build());

            List<Element<Direction>> expected = service.extractPartyResponse(LOCAL_AUTHORITY.getValue(), directions);

            assertThat(expected.get(0).getValue().getResponse()).isNull();
        }

        @Test
        void shouldSetResponseToNullWhenRoleDoesNotMatch() {
            List<Element<Direction>> directions = ImmutableList.of(Element.<Direction>builder()
                .id(uuid)
                .value(Direction.builder()
                    .responses(ImmutableList.of(Element.<DirectionResponse>builder()
                        .value(DirectionResponse.builder()
                            .assignee(CAFCASS)
                            .directionId(uuid)
                            .build())
                        .build()))
                    .build())
                .build());

            List<Element<Direction>> expected = service.extractPartyResponse(LOCAL_AUTHORITY.getValue(), directions);

            assertThat(expected.get(0).getValue().getResponse()).isNull();
        }

        @Test
        void shouldSetResponseToNullWhenDirectionIdAndRoleDoesNotMatch() {
            List<Element<Direction>> directions = ImmutableList.of(Element.<Direction>builder()
                .id(uuid)
                .value(Direction.builder()
                    .responses(ImmutableList.of(Element.<DirectionResponse>builder()
                        .value(DirectionResponse.builder()
                            .assignee(CAFCASS)
                            .directionId(UUID.randomUUID())
                            .build())
                        .build()))
                    .build())
                .build());

            List<Element<Direction>> expected = service.extractPartyResponse(LOCAL_AUTHORITY.getValue(), directions);

            assertThat(expected.get(0).getValue().getResponse()).isNull();
        }

        @Test
        void shouldOnlyExtractPartyResponsesForGivenPartyWhenManyResponses() {
            List<Element<Direction>> directions = ImmutableList.of(Element.<Direction>builder()
                .id(uuid)
                .value(Direction.builder()
                    .responses(ImmutableList.of(Element.<DirectionResponse>builder()
                            .value(DirectionResponse.builder()
                                .assignee(CAFCASS)
                                .directionId(uuid)
                                .build())
                            .build(),
                        Element.<DirectionResponse>builder()
                            .value(DirectionResponse.builder()
                                .assignee(LOCAL_AUTHORITY)
                                .directionId(uuid)
                                .build())
                            .build()
                    ))
                    .build())
                .build());

            List<Element<Direction>> expected = service.extractPartyResponse(LOCAL_AUTHORITY.getValue(), directions);

            assertThat(expected.get(0).getValue().getResponse().getAssignee()).isEqualTo(LOCAL_AUTHORITY);
        }

        @Test
        void shouldPlaceResponsesWhenMultipleDirections() {
            UUID otherUuid = UUID.randomUUID();

            List<Element<Direction>> directions = ImmutableList.of(
                Element.<Direction>builder()
                    .id(uuid)
                    .value(Direction.builder()
                        .responses(ImmutableList.of(Element.<DirectionResponse>builder()
                            .value(DirectionResponse.builder()
                                .assignee(LOCAL_AUTHORITY)
                                .directionId(uuid)
                                .build())
                            .build()))
                        .build())
                    .build(),
                Element.<Direction>builder()
                    .id(otherUuid)
                    .value(Direction.builder()
                        .responses(ImmutableList.of(Element.<DirectionResponse>builder()
                            .value(DirectionResponse.builder()
                                .assignee(LOCAL_AUTHORITY)
                                .directionId(otherUuid)
                                .build())
                            .build()))
                        .build())
                    .build());

            List<Element<Direction>> expected = service.extractPartyResponse(LOCAL_AUTHORITY.getValue(), directions);

            assertThat(expected.get(0).getValue().getResponse()).isNotNull();
            assertThat(expected.get(1).getValue().getResponse()).isNotNull();
            assertThat(expected).hasSize(2);
        }
    }

    @Nested
    class CollectDirectionsToMap {

        @Test
        void shouldReturnMapWithEmptyListWhenNoDirectionsForAssignee() {
            Map<DirectionAssignee, List<Element<Direction>>> map =
                service.collectDirectionsToMap(CaseData.builder().build());

            assertThat(new ArrayList<>(map.values()))
                .isEqualTo(ImmutableList.of(EMPTY_LIST, EMPTY_LIST, EMPTY_LIST, EMPTY_LIST, EMPTY_LIST, EMPTY_LIST));
        }

        @Test
        void shouldReturnMapWithDirectionListWhenDirectionsForAssignees() {
            Map<DirectionAssignee, List<Element<Direction>>> map =
                service.collectDirectionsToMap(populateCaseDataWithFixedDirections()
                    .courtDirectionsCustom(buildDirections(COURT))
                    .build());

            Map<DirectionAssignee, List<Element<Direction>>> expectedMap = Stream.of(DirectionAssignee.values())
                .collect(toMap(directionAssignee ->
                    directionAssignee, DirectionHelperServiceTest.this::buildDirections));

            assertThat(map).isEqualTo(expectedMap);
        }
    }

    @Nested
    class GetResponses {
        final UUID uuid = UUID.randomUUID();

        @Test
        void shouldAddCorrectAssigneeAndDirectionToResponseWhenResponseExists() {
            String complied = "Yes";

            List<DirectionResponse> responses = service.getResponses(
                ImmutableMap.of(LOCAL_AUTHORITY, buildDirection(LOCAL_AUTHORITY, uuid, complied)));

            assertThat(responses.get(0).getAssignee()).isEqualTo(LOCAL_AUTHORITY);
            assertThat(responses.get(0).getDirectionId()).isEqualTo(uuid);
        }

        @Test
        void shouldNotReturnResponseWhenCompliedHasNotBeenAnswered() {
            String complied = null;

            List<DirectionResponse> responses = service.getResponses(
                ImmutableMap.of(LOCAL_AUTHORITY, buildDirection(LOCAL_AUTHORITY, uuid, complied)));

            assertThat(responses).isEmpty();
        }

        @Test
        void shouldNotReturnResponseWhenNoResponseExists() {
            List<DirectionResponse> responses = service.getResponses(
                ImmutableMap.of(LOCAL_AUTHORITY, ImmutableList.of(Element.<Direction>builder()
                    .value(Direction.builder()
                        .directionText("Direction")
                        .build())
                    .build())));

            assertThat(responses).isEmpty();
        }

        @Test
        void shouldAddCorrectAssigneeAndDirectionWhenMultipleDifferentResponsesExist() {
            String complied = "Yes";
            UUID otherUuid = UUID.randomUUID();

            List<DirectionResponse> responses = service.getResponses(
                ImmutableMap.of(
                    LOCAL_AUTHORITY, buildDirection(LOCAL_AUTHORITY, uuid, complied),
                    CAFCASS, buildDirection(CAFCASS, otherUuid, complied)
                ));

            assertThat(responses.get(0).getAssignee()).isEqualTo(LOCAL_AUTHORITY);
            assertThat(responses.get(0).getDirectionId()).isEqualTo(uuid);
            assertThat(responses.get(1).getAssignee()).isEqualTo(CAFCASS);
            assertThat(responses.get(1).getDirectionId()).isEqualTo(otherUuid);
        }

        @Test
        void shouldAddCorrectAssigneeAndDirectionWhenSameDirectionWithValidResponses() {
            String complied = "Yes";

            List<DirectionResponse> responses = service.getResponses(
                ImmutableMap.of(
                    LOCAL_AUTHORITY, buildDirection(LOCAL_AUTHORITY, uuid, complied),
                    CAFCASS, buildDirection(CAFCASS, uuid, complied)
                ));

            assertThat(responses.get(0).getAssignee()).isEqualTo(LOCAL_AUTHORITY);
            assertThat(responses.get(0).getDirectionId()).isEqualTo(uuid);
            assertThat(responses.get(1).getAssignee()).isEqualTo(CAFCASS);
            assertThat(responses.get(1).getDirectionId()).isEqualTo(uuid);
        }

        private List<Element<Direction>> buildDirection(DirectionAssignee assignee, UUID id, String complied) {
            return Lists.newArrayList(Element.<Direction>builder()
                .id(id)
                .value(Direction.builder()
                    .directionType("direction")
                    .directionText("example direction text")
                    .assignee(assignee)
                    .response(DirectionResponse.builder()
                        .complied(complied)
                        .build())
                    .build())
                .build());
        }
    }

    @Nested
    class SortDirectionsByAssignee {

        @Test
        void shouldSortDirectionsIntoSeparateEntriesInMapWhenSingleAssignee() {
            List<Element<Direction>> directions = ImmutableList.<Element<Direction>>builder()
                .addAll(buildDirections(LOCAL_AUTHORITY))
                .build();

            Map<String, List<Element<Direction>>> sortedDirections = service.sortDirectionsByAssignee(directions);

            assertThat(sortedDirections).containsOnlyKeys(LOCAL_AUTHORITY.getValue());
        }

        @Test
        void shouldSortDirectionsIntoSeparateEntriesInMapWhenManyAssignees() {
            List<Element<Direction>> directions = ImmutableList.<Element<Direction>>builder()
                .addAll(buildDirections(LOCAL_AUTHORITY))
                .addAll(buildDirections(COURT))
                .build();

            Map<String, List<Element<Direction>>> sortedDirections = service.sortDirectionsByAssignee(directions);

            assertThat(sortedDirections).containsOnlyKeys(LOCAL_AUTHORITY.getValue(), COURT.getValue());
        }
    }

    @Nested
    class RemoveCustomDirections {

        @Test
        void shouldRemoveCustomDirectionFromListWhenCustomFlagIsYes() {
            List<Element<Direction>> filteredDirections = service.removeCustomDirections(buildCustomDirections());

            assertThat(filteredDirections).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"No"})
        void shouldNotRemoveDirectionFromListWhenCustomFlagIsNo(String custom) {
            List<Element<Direction>> filteredDirections = service.removeCustomDirections(ImmutableList.of(
                Element.<Direction>builder()
                    .value(Direction.builder()
                        .custom(custom)
                        .build())
                    .build()));

            assertThat(filteredDirections).hasSize(1);
        }
    }

    @Nested
    class GetDirectionsForAssignee {

        @Test
        void shouldFilterAListOfDirectionsWhenDirectionsForSingleAssignee() {
            List<Element<Direction>> directions = new ArrayList<>(buildDirections(LOCAL_AUTHORITY));

            List<Element<Direction>> returnedDirections = service.getDirectionsForAssignee(directions, LOCAL_AUTHORITY);

            assertThat(returnedDirections).hasSize(1);
            assertThat(returnedDirections).isEqualTo(buildDirections(LOCAL_AUTHORITY));
        }

        @Test
        void shouldFilterAListOfDirectionsWhenDirectionsForManyAssignees() {
            List<Element<Direction>> directions = new ArrayList<>();
            directions.addAll(buildDirections(LOCAL_AUTHORITY));
            directions.addAll(buildDirections(CAFCASS));
            directions.addAll(buildDirections(COURT));

            List<Element<Direction>> returnedDirections = service.getDirectionsForAssignee(directions, LOCAL_AUTHORITY);

            assertThat(returnedDirections).hasSize(1);
            assertThat(returnedDirections).isEqualTo(buildDirections(LOCAL_AUTHORITY));
        }

        @Test
        void shouldReturnEmptyListOfDirectionsWhenNoneExistForAssignee() {
            List<Element<Direction>> directions = new ArrayList<>();

            List<Element<Direction>> returnedDirections = service.getDirectionsForAssignee(directions, LOCAL_AUTHORITY);

            assertThat(returnedDirections).isEmpty();
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
                LOCAL_AUTHORITY.getValue(), buildDirections(LOCAL_AUTHORITY), caseDetails);

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
                LOCAL_AUTHORITY.getValue(), buildDirections(LOCAL_AUTHORITY), caseDetails);

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
                LOCAL_AUTHORITY.getValue(), buildDirections(LOCAL_AUTHORITY), caseDetails);

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

            service.addAssigneeDirectionKeyValuePairsToCaseData(LOCAL_AUTHORITY.getValue(), directions, caseDetails);

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

            service.addAssigneeDirectionKeyValuePairsToCaseData(COURT.getValue(), directions, caseDetails);

            assertThat(caseDetails.getData()).hasSize(1)
                .extracting("courtDirectionsCustom")
                .isEqualTo(directions);
        }

        private List<Element<Direction>> expectedDirection() {
            return buildDirections(LOCAL_AUTHORITY);
        }
    }

    private CaseData.CaseDataBuilder populateCaseDataWithFixedDirections() {
        return CaseData.builder()
            .allParties(buildDirections(ALL_PARTIES))
            .localAuthorityDirections(buildDirections(LOCAL_AUTHORITY))
            .parentsAndRespondentsDirections(buildDirections(PARENTS_AND_RESPONDENTS))
            .cafcassDirections(buildDirections(CAFCASS))
            .otherPartiesDirections(buildDirections(OTHERS))
            .courtDirections(buildDirections(COURT));
    }

    private List<Element<Direction>> buildDirections(DirectionAssignee assignee) {
        return Lists.newArrayList(Element.<Direction>builder()
            .value(Direction.builder()
                .directionType("direction")
                .directionText("example direction text")
                .assignee(assignee)
                .build())
            .build());
    }

    private List<Element<Direction>> buildCustomDirections() {
        return Lists.newArrayList(Element.<Direction>builder()
            .value(Direction.builder()
                .directionType("direction")
                .directionText("example direction text")
                .custom("Yes")
                .build())
            .build());
    }
}
