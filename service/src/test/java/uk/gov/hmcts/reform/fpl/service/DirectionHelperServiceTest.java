package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.DirectionResponse;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.Display;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
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

    @Nested
    class CombineAllDirectionsTest {

        @Test
        void shouldAddRoleDirectionsIntoOneListWhenInSeparateCollections() {
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
        void shouldAllowNullCustomDirectionValuesWhenCollectingToSingleList() {
            CaseData caseData = populateCaseDataWithFixedDirections().build();

            List<Element<Direction>> directions = service.combineAllDirections(caseData);

            assertThat(directions).size().isEqualTo(6);
        }

        @Test
        void shouldAddCustomFlagOnlyToCustomDirectionsWhenCalledWithCustomDirections() {
            CaseData caseData = populateCaseDataWithFixedDirections()
                .courtDirectionsCustom(buildCustomDirections())
                .build();

            List<Element<Direction>> directions = service.combineAllDirections(caseData);

            List<Element<Direction>> directionWithCustomFlag = directions.stream()
                .filter(element -> element.getValue().getCustom() != null
                    && element.getValue().getCustom().equals("Yes"))
                .collect(toList());

            assertThat(directionWithCustomFlag).hasSize(1);
        }

        @Test
        void shouldAssignCustomDirectionToCorrectAssigneeWhenAddedToSingleList() {
            CaseData caseData = populateCaseDataWithFixedDirections()
                .courtDirectionsCustom(buildCustomDirections())
                .build();

            List<Element<Direction>> directions = service.combineAllDirections(caseData);

            List<Element<Direction>> courtDirections = directions.stream()
                .filter(element -> element.getValue().getAssignee().equals(COURT))
                .collect(toList());

            assertThat(courtDirections).hasSize(2);
        }
    }

    @Nested
    class PersistHiddenDirectionValuesTest {

        @Test
        void shouldAddValuesHiddenInCcdUiIncludingTextWhenReadOnlyIsYes() {
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
        void shouldAddValuesHiddenInCcdUiExcludingTextWhenReadOnlyIsNo() {
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
            List<Element<DirectionResponse>> responses = new ArrayList<>();

            List<Element<Direction>> directionWithNoResponse = ImmutableList.of(
                Element.<Direction>builder()
                    .id(uuid)
                    .value(Direction.builder()
                        .responses(responses)
                        .build())
                    .build());

            List<Element<Direction>> directionWithResponse = ImmutableList.of(
                Element.<Direction>builder()
                    .id(uuid)
                    .value(Direction.builder()
                        .response(DirectionResponse.builder()
                            .complied("Yes")
                            .directionId(uuid)
                            .build())
                        .build())
                    .build());

            service.addResponsesToDirections(directionWithResponse, directionWithNoResponse);

            assertThat(getResponses(directionWithNoResponse)).isNotEmpty();
        }

        @Test
        void shouldNotAddResponseToDirectionWhenMatchingResponseAlreadyExist() {
            DirectionResponse directionResponse = DirectionResponse.builder()
                .complied("Yes")
                .assignee(LOCAL_AUTHORITY)
                .directionId(uuid)
                .build();

            List<Element<DirectionResponse>> responses = new ArrayList<>();
            responses.add(Element.<DirectionResponse>builder()
                .value(directionResponse)
                .build());

            List<Element<Direction>> directionWithNoResponse = ImmutableList.of(
                Element.<Direction>builder()
                    .id(uuid)
                    .value(Direction.builder()
                        .directionType("Direction")
                        .assignee(LOCAL_AUTHORITY)
                        .responses(responses)
                        .build())
                    .build());

            List<Element<Direction>> directionWithResponse = ImmutableList.of(
                Element.<Direction>builder()
                    .id(uuid)
                    .value(Direction.builder()
                        .directionType("Direction")
                        .assignee(LOCAL_AUTHORITY)
                        .response(directionResponse)
                        .build())
                    .build());

            service.addResponsesToDirections(directionWithResponse, directionWithNoResponse);

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

            List<Element<Direction>> directionWithOldResponse = ImmutableList.of(
                Element.<Direction>builder()
                    .id(uuid)
                    .value(Direction.builder()
                        .directionType("Direction")
                        .assignee(LOCAL_AUTHORITY)
                        .responses(responses)
                        .build())
                    .build());

            List<Element<Direction>> directionWithNewResponse = ImmutableList.of(
                Element.<Direction>builder()
                    .id(uuid)
                    .value(Direction.builder()
                        .directionType("Direction")
                        .assignee(LOCAL_AUTHORITY)
                        .response(DirectionResponse.builder()
                            .assignee(LOCAL_AUTHORITY)
                            .complied("Yes")
                            .directionId(uuid)
                            .build())
                        .build())
                    .build());

            service.addResponsesToDirections(directionWithNewResponse, directionWithOldResponse);

            assertThat(getResponses(directionWithOldResponse)).hasSize(1);
            assertThat(getResponses(directionWithOldResponse).get(0).getValue().getComplied().equals("Yes"));
        }

        @Test
        void shouldAddMultipleResponsesForTheSameDirectionWhenDifferentPartiesComply() {
            List<Element<DirectionResponse>> responses = new ArrayList<>();

            List<Element<Direction>> directionsWithNoResponse = ImmutableList.of(
                Element.<Direction>builder()
                    .id(uuid)
                    .value(Direction.builder()
                        .assignee(ALL_PARTIES)
                        .responses(responses)
                        .build())
                    .build());

            List<Element<Direction>> directionsWithResponse = ImmutableList.of(
                Element.<Direction>builder()
                    .id(uuid)
                    .value(Direction.builder()
                        .assignee(ALL_PARTIES)
                        .response(DirectionResponse.builder()
                            .assignee(LOCAL_AUTHORITY)
                            .complied("Yes")
                            .directionId(uuid)
                            .build())
                        .build())
                    .build(),
                Element.<Direction>builder()
                    .id(uuid)
                    .value(Direction.builder()
                        .assignee(ALL_PARTIES)
                        .response(DirectionResponse.builder()
                            .assignee(CAFCASS)
                            .complied("Yes")
                            .directionId(uuid)
                            .build())
                        .build())
                    .build());

            service.addResponsesToDirections(directionsWithResponse, directionsWithNoResponse);

            assertThat(getResponses(directionsWithNoResponse)).hasSize(2);
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
