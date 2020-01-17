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
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.Display;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
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
class CommonDirectionServiceTest {
    private static final UUID DIRECTION_ID = randomUUID();

    private CommonDirectionService service = new CommonDirectionService();

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

        DirectionConfiguration directionConfig = getDirectionConfig();

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
        DirectionConfiguration directionConfig = getDirectionConfig();

        Element<Direction> actualDirection = service.constructDirectionForCCD(directionConfig, null);

        assertThat(actualDirection.getValue()).isEqualTo(Direction.builder()
            .directionType("direction title")
            .directionText("direction text")
            .readOnly("No")
            .directionRemovable("No")
            .dateToBeCompletedBy(null)
            .assignee(LOCAL_AUTHORITY)
            .build());
    }

    private DirectionConfiguration getDirectionConfig() {
        return DirectionConfiguration.builder()
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
                    directionAssignee, CommonDirectionServiceTest.this::buildDirections));

            AtomicReference<List<Direction>> directionsFromMap = new AtomicReference<>();
            AtomicReference<List<Direction>> expectedDirections = new AtomicReference<>();

            map.forEach((key, value) -> {
                directionsFromMap.set(value.stream().map(Element::getValue).collect(toList()));
                expectedDirections.set(expectedMap.get(key).stream().map(Element::getValue).collect(toList()));

                assertThat(directionsFromMap.get()).isEqualTo(expectedDirections.get());
            });
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
    class AddEmptyDirectionsForAssigneeNotInMap {

        @Test
        void shouldAddEmptyListValueWhenKeyNotPresentInMap() {
            Map<DirectionAssignee, List<Element<Direction>>> map = new HashMap<>();

            service.addEmptyDirectionsForAssigneeNotInMap(map);

            Stream.of(DirectionAssignee.values())
                .forEach(assignee -> assertThat(map.get(assignee)).isEqualTo(emptyList()));
        }

        @Test
        void shouldAddEmptyListValueToNewKeysWhenSomeKeysAreAlreadyPresent() {
            Map<DirectionAssignee, List<Element<Direction>>> map = new HashMap<>();
            map.put(LOCAL_AUTHORITY, emptyListOfElement());
            map.put(CAFCASS, emptyListOfElement());

            service.addEmptyDirectionsForAssigneeNotInMap(map);

            assertThat(map).containsOnlyKeys(DirectionAssignee.values());
            assertThat(map).extracting(ALL_PARTIES, PARENTS_AND_RESPONDENTS, COURT, OTHERS).containsOnly(emptyList());
            assertThat(map).extracting(LOCAL_AUTHORITY, CAFCASS).containsOnly(emptyListOfElement());
        }

        private List<Element<Direction>> emptyListOfElement() {
            return List.of(Element.<Direction>builder().build());
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
            List<Element<Direction>> filteredDirections = service.removeCustomDirections(List.of(
                ElementUtils.element(Direction.builder()
                    .custom(custom)
                    .build())));

            assertThat(filteredDirections).hasSize(1);
        }
    }

    @Nested
    class GetDirectionsForAssignee {

        @Test
        void shouldFilterAListOfDirectionsWhenDirectionsForSingleAssignee() {
            List<Element<Direction>> directions = new ArrayList<>(buildDirections(LOCAL_AUTHORITY, DIRECTION_ID));

            List<Element<Direction>> returnedDirections = service.getDirectionsForAssignee(directions, LOCAL_AUTHORITY);

            assertThat(returnedDirections).hasSize(1);
            assertThat(returnedDirections).isEqualTo(buildDirections(LOCAL_AUTHORITY, DIRECTION_ID));
        }

        @Test
        void shouldFilterAListOfDirectionsWhenDirectionsForManyAssignees() {
            List<Element<Direction>> directions = new ArrayList<>();
            directions.addAll(buildDirections(LOCAL_AUTHORITY, DIRECTION_ID));
            directions.addAll(buildDirections(CAFCASS));
            directions.addAll(buildDirections(COURT));

            List<Element<Direction>> returnedDirections = service.getDirectionsForAssignee(directions, LOCAL_AUTHORITY);

            assertThat(returnedDirections).hasSize(1);
            assertThat(returnedDirections).isEqualTo(buildDirections(LOCAL_AUTHORITY, DIRECTION_ID));
        }

        @Test
        void shouldReturnEmptyListOfDirectionsWhenNoneExistForAssignee() {
            List<Element<Direction>> directions = new ArrayList<>();

            List<Element<Direction>> returnedDirections = service.getDirectionsForAssignee(directions, LOCAL_AUTHORITY);

            assertThat(returnedDirections).isEmpty();
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
            return List.of(ElementUtils.element(CaseManagementOrder.builder()
                .directions(cmoDirections)
                .build()));
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
        return Lists.newArrayList(ElementUtils.element(Direction.builder()
            .directionType("direction")
            .directionText("example direction text")
            .assignee(assignee)
            .build()));
    }

    private List<Element<Direction>> buildDirections(DirectionAssignee assignee, UUID directionId) {
        return Lists.newArrayList(ElementUtils.element(directionId, Direction.builder()
            .directionType("direction")
            .directionText("example direction text")
            .assignee(assignee)
            .build()));
    }

    private List<Element<Direction>> buildCustomDirections() {
        return Lists.newArrayList(ElementUtils.element(
            Direction.builder()
                .directionType("direction")
                .directionText("example direction text")
                .custom("Yes")
                .build()));
    }
}
