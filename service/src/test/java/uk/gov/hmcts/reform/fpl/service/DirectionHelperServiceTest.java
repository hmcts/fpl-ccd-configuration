package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.DirectionResponse;
import uk.gov.hmcts.reform.fpl.model.Order;
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
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.ComplyOnBehalfEvent.COMPLY_ON_BEHALF_SDO;
import static uk.gov.hmcts.reform.fpl.enums.ComplyOnBehalfEvent.COMPLY_OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;

@ExtendWith(SpringExtension.class)
class DirectionHelperServiceTest {

    @MockBean
    private UserDetailsService userDetailsService;

    private DirectionHelperService service;

    @BeforeEach
    void setUp() {
        service = new DirectionHelperService(userDetailsService);
    }

    @Test
    void combineAllDirections_shouldAddRoleDirectionsIntoOneList() {
        CaseData caseData = populateCaseDataWithFixedDirections()
            .allPartiesCustom(buildCustomDirections())
            .localAuthorityDirectionsCustom(buildCustomDirections())
            .respondentDirectionsCustom(buildCustomDirections())
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
        UUID uuid = randomUUID();

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
        UUID uuid = randomUUID();

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
            assertTrue(compliedFieldHasBeenUpdatedToYes(directionWithOldResponse));
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
            assertTrue(compliedFieldHasBeenUpdatedToYes(directions));
        }

        @Test
        void shouldAddAnotherResponseWhenDifferentRespondingOnBehalfOfValue() {
            List<Element<DirectionResponse>> responses = new ArrayList<>();
            responses.add(Element.<DirectionResponse>builder()
                .value(DirectionResponse.builder()
                    .assignee(COURT)
                    .respondingOnBehalfOf("CAFCASS")
                    .complied("No")
                    .directionId(uuid)
                    .build())
                .build());

            List<Element<Direction>> directions = getDirectionsWithResponses(responses);

            List<DirectionResponse> newResponses = ImmutableList.of(DirectionResponse.builder()
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
            List<Element<DirectionResponse>> responses = new ArrayList<>();
            responses.add(Element.<DirectionResponse>builder()
                .value(DirectionResponse.builder()
                    .assignee(COURT)
                    .respondingOnBehalfOf("CAFCASS")
                    .complied("No")
                    .directionId(uuid)
                    .build())
                .build());

            List<Element<Direction>> directions = getDirectionsWithResponses(responses);

            List<DirectionResponse> newResponses = ImmutableList.of(DirectionResponse.builder()
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

        private boolean compliedFieldHasBeenUpdatedToYes(List<Element<Direction>> directions) {
            return getResponses(directions).get(0).getValue().getComplied().equals("Yes");
        }
    }

    @Nested
    class ExtractPartyResponse {
        private final UUID uuid = randomUUID();

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

            List<Element<Direction>> expected = service.extractPartyResponse(LOCAL_AUTHORITY, directions);

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
                            .directionId(randomUUID())
                            .build())
                        .build()))
                    .build())
                .build());

            List<Element<Direction>> expected = service.extractPartyResponse(LOCAL_AUTHORITY, directions);

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

            List<Element<Direction>> expected = service.extractPartyResponse(LOCAL_AUTHORITY, directions);

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
                            .directionId(randomUUID())
                            .build())
                        .build()))
                    .build())
                .build());

            List<Element<Direction>> expected = service.extractPartyResponse(LOCAL_AUTHORITY, directions);

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

            List<Element<Direction>> expected = service.extractPartyResponse(LOCAL_AUTHORITY, directions);

            assertThat(expected.get(0).getValue().getResponse().getAssignee()).isEqualTo(LOCAL_AUTHORITY);
        }

        @Test
        void shouldPlaceResponsesWhenMultipleDirections() {
            UUID otherUuid = randomUUID();

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

            List<Element<Direction>> expected = service.extractPartyResponse(LOCAL_AUTHORITY, directions);

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
        final UUID uuid = randomUUID();

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
            UUID otherUuid = randomUUID();

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

            Map<DirectionAssignee, List<Element<Direction>>> sortedDirections = service.sortDirectionsByAssignee(
                directions);

            assertThat(sortedDirections).containsOnlyKeys(LOCAL_AUTHORITY);
        }

        @Test
        void shouldSortDirectionsIntoSeparateEntriesInMapWhenManyAssignees() {
            List<Element<Direction>> directions = ImmutableList.<Element<Direction>>builder()
                .addAll(buildDirections(LOCAL_AUTHORITY))
                .addAll(buildDirections(COURT))
                .build();

            Map<DirectionAssignee, List<Element<Direction>>> sortedDirections = service.sortDirectionsByAssignee(
                directions);

            assertThat(sortedDirections).containsOnlyKeys(LOCAL_AUTHORITY, COURT);
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
            List<Element<DirectionResponse>> responses = new ArrayList<>();
            responses.add(Element.<DirectionResponse>builder()
                .value(DirectionResponse.builder()
                    .assignee(assignee)
                    .respondingOnBehalfOf(onBehalfOf)
                    .build())
                .build());
            return responses;
        }

        private List<Element<Direction>> createDirectionWithResponses(List<Element<DirectionResponse>> responses) {
            List<Element<Direction>> directions = new ArrayList<>();
            directions.add(Element.<Direction>builder()
                .value(Direction.builder()
                    .responses(responses)
                    .build())
                .build());
            return directions;
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

        private List<Element<Direction>> createDirections(UUID directionId,
                                                          List<Element<DirectionResponse>> responses) {
            List<Element<Direction>> directions = new ArrayList<>();
            directions.add(Element.<Direction>builder()
                .id(directionId)
                .value(Direction.builder()
                    .responses(responses)
                    .build())
                .build());
            return directions;
        }

        private List<Element<DirectionResponse>> createDirectionResponses(UUID responseId, UUID directionId) {
            List<Element<DirectionResponse>> responses = new ArrayList<>();
            responses.add(Element.<DirectionResponse>builder()
                .id(responseId)
                .value(DirectionResponse.builder()
                    .directionId(directionId)
                    .build())
                .build());
            return responses;
        }
    }

    @Nested
    class AddDirectionsToCaseDetails {

        @Test
        void shouldDoNothingWhenDirectionsDoNotNeedToBePopulatedForAllParties() {
            CaseDetails caseDetails = CaseDetails.builder().build();
            Map<DirectionAssignee, List<Element<Direction>>> directionsMap = new HashMap<>();
            directionsMap.put(ALL_PARTIES, buildDirections(ALL_PARTIES));

            service.addDirectionsToCaseDetails(caseDetails, directionsMap);

            assertThat(caseDetails).isEqualTo(CaseDetails.builder().build());
            assertThat(directionsMap).isEqualTo(ImmutableMap.of(ALL_PARTIES, buildDirections(ALL_PARTIES)));
        }

        @Test
        void shouldDoNothingWhenDirectionsDoNotNeedToBePopulatedForCourt() {
            CaseDetails caseDetails = CaseDetails.builder().build();
            Map<DirectionAssignee, List<Element<Direction>>> directionsMap = new HashMap<>();
            directionsMap.put(COURT, buildDirections(COURT));
            directionsMap.put(ALL_PARTIES, buildDirections(ALL_PARTIES));

            service.addDirectionsToCaseDetails(caseDetails, directionsMap);

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

            service.addDirectionsToCaseDetails(caseDetails, directionsMap);

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

            service.addDirectionsToCaseDetails(caseDetails, directionsMap);

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

            service.addDirectionsToCaseDetails(caseDetails, directionsMap);

            List<Element<Direction>> expectedDirections = new ArrayList<>();
            expectedDirections.addAll(buildDirections(PARENTS_AND_RESPONDENTS));
            expectedDirections.addAll(allPartyDirections());

            assertThat(caseDetails).isEqualTo(CaseDetails.builder()
                .data(ImmutableMap.of(PARENTS_AND_RESPONDENTS.getValue().concat("Custom"), expectedDirections))
                .build());
        }

        private List<Element<Direction>> allPartyDirections() {
            List<Element<DirectionResponse>> responses = responsesForRespondent();

            return buildDirections(ALL_PARTIES).stream()
                .map(element -> element.getValue().toBuilder()
                    .responses(responses)
                    .build())
                .map(direction -> Element.<Direction>builder().value(direction).build())
                .collect(toList());
        }

        private List<Element<DirectionResponse>> responsesForRespondent() {
            List<Element<DirectionResponse>> responses = new ArrayList<>();
            responses.add(Element.<DirectionResponse>builder()
                .value(DirectionResponse.builder()
                    .assignee(COURT)
                    .respondingOnBehalfOf("RESPONDENT_1")
                    .complied("Yes")
                    .build())
                .build());
            return responses;
        }
    }

    @Nested
    class AddComplyOnBehalfResponsesToDirectionsInStandardDirectionOrder {

        @Test
        void shouldAddCafcassResponseWhenValidResponseMadeByCourt() {
            UUID directionId = randomUUID();
            Order sdo = orderWithCafcassDirection(directionId);
            List<Element<Direction>> directionWithResponse = directionWithCafcassResponse(directionId);

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

            service.addComplyOnBehalfResponsesToDirectionsInOrder(caseData, COMPLY_ON_BEHALF_SDO, "auth");

            assertThat(getResponses(caseData).get(0).getValue()).isEqualTo(expectedResponse);
        }

        @Test
        void shouldAddResponseForOtherPartiesWhenValidResponseMadeByCourt() {
            UUID directionId = randomUUID();
            UUID responseId = randomUUID();
            Direction.DirectionBuilder direction = Direction.builder().assignee(OTHERS);

            DirectionResponse.DirectionResponseBuilder response = DirectionResponse.builder()
                .complied("Yes")
                .respondingOnBehalfOf("OTHERS_1");

            CaseData caseData = prepareCaseData(directionId, direction, createResponses(responseId, response));

            List<Element<DirectionResponse>> expectedResponses = ImmutableList.of(Element.<DirectionResponse>builder()
                .id(responseId)
                .value(response
                    .directionId(directionId)
                    .assignee(COURT)
                    .build())
                .build());

            service.addComplyOnBehalfResponsesToDirectionsInOrder(caseData, COMPLY_ON_BEHALF_SDO, "auth");

            assertThat(getResponses(caseData)).containsAll(expectedResponses);
        }

        @Test
        void shouldAddResponseForOtherPartiesWhenValidResponseMadeBySolicitor() {
            given(userDetailsService.getUserName("auth")).willReturn("Emma Taylor");

            UUID directionId = randomUUID();
            UUID responseId = randomUUID();
            Direction.DirectionBuilder direction = Direction.builder().assignee(OTHERS);

            DirectionResponse.DirectionResponseBuilder response = DirectionResponse.builder()
                .complied("Yes")
                .respondingOnBehalfOf("OTHERS_1");

            CaseData caseData = prepareCaseData(directionId, direction, createResponses(responseId, response));

            List<Element<DirectionResponse>> expectedResponses = ImmutableList.of(Element.<DirectionResponse>builder()
                .id(responseId)
                .value(response
                    .directionId(directionId)
                    .assignee(OTHERS)
                    .responder("Emma Taylor")
                    .build())
                .build());

            service.addComplyOnBehalfResponsesToDirectionsInOrder(caseData, COMPLY_OTHERS, "auth");

            assertThat(getResponses(caseData)).containsAll(expectedResponses);
        }

        private List<Element<Direction>> directionWithCafcassResponse(UUID directionId) {
            return ImmutableList.of(Element.<Direction>builder()
                .id(directionId)
                .value(Direction.builder()
                    .response(DirectionResponse.builder()
                        .directionId(directionId)
                        .assignee(CAFCASS)
                        .complied("Yes")
                        .build())
                    .build())
                .build());
        }

        private Order orderWithCafcassDirection(UUID directionId) {
            return Order.builder()
                .directions(ImmutableList.of(Element.<Direction>builder()
                    .id(directionId)
                    .value(Direction.builder()
                        .directionType("example direction")
                        .assignee(CAFCASS)
                        .build())
                    .build()))
                .build();
        }


        private CaseData prepareCaseData(UUID directionId,
                                         Direction.DirectionBuilder direction,
                                         List<Element<DirectionResponse>> responses) {
            return CaseData.builder()
                .standardDirectionOrder(Order.builder()
                    .directions(ImmutableList.of(Element.<Direction>builder()
                        .id(directionId)
                        .value(direction.build())
                        .build()))
                    .build())
                .otherPartiesDirectionsCustom(ImmutableList.of(Element.<Direction>builder()
                    .id(directionId)
                    .value(direction.responses(responses).build())
                    .build()))
                .build();
        }

        private List<Element<DirectionResponse>> createResponses(UUID responseId,
                                                                 DirectionResponse.DirectionResponseBuilder response) {
            List<Element<DirectionResponse>> responses = new ArrayList<>();
            responses.add(Element.<DirectionResponse>builder()
                .id(responseId)
                .value(response.build())
                .build());

            return responses;
        }

        private List<Element<DirectionResponse>> getResponses(CaseData caseData) {
            return caseData.getStandardDirectionOrder().getDirections().get(0).getValue().getResponses();
        }
    }

    @Nested
    class GetDirectionsToComplyWith {

        @Test
        void shouldReturnStandardDirectionOrderDirectionsWhenServedCaseManagementOrdersIsEmpty() {
            List<Element<Direction>> sdoDirections = buildDirections(LOCAL_AUTHORITY);
            CaseData caseData = caseDataWithSdo(sdoDirections)
                .servedCaseManagementOrders(emptyList())
                .build();

            List<Element<Direction>> directions = service.getDirectionsToComplyWith(caseData);

            assertThat(directions).isEqualTo(sdoDirections);
        }

        @Test
        void shouldReturnCaseManagementOrderDirectionsWhenServedCaseManagementOrdersIsNotEmpty() {
            List<Element<Direction>> cmoDirections = buildDirections(LOCAL_AUTHORITY);
            CaseData caseData = caseDataWithSdo(buildDirections(CAFCASS))
                .servedCaseManagementOrders(servedCaseManagementOrder(cmoDirections))
                .build();

            List<Element<Direction>> directions = service.getDirectionsToComplyWith(caseData);

            assertThat(directions).isEqualTo(cmoDirections);
        }

        private CaseData.CaseDataBuilder caseDataWithSdo(List<Element<Direction>> sdoDirections) {
            return CaseData.builder()
                .standardDirectionOrder(
                    Order.builder()
                        .directions(sdoDirections)
                        .build());
        }

        private List<Element<CaseManagementOrder>> servedCaseManagementOrder(List<Element<Direction>> cmoDirections) {
            return ImmutableList.of(Element.<CaseManagementOrder>builder()
                .value(CaseManagementOrder.builder()
                    .directions(cmoDirections)
                    .build())
                .build());
        }
    }

    private CaseData.CaseDataBuilder populateCaseDataWithFixedDirections() {
        return CaseData.builder()
            .allParties(buildDirections(ALL_PARTIES))
            .localAuthorityDirections(buildDirections(LOCAL_AUTHORITY))
            .respondentDirections(buildDirections(PARENTS_AND_RESPONDENTS))
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
