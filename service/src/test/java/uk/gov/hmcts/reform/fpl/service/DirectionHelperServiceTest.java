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
import uk.gov.hmcts.reform.fpl.model.Compliance;
import uk.gov.hmcts.reform.fpl.model.Direction;
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

    @Nested
    class SortDirectionsByAssignee {

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
        void shouldAddResponsesToDirectionWhenResponsesForDirectionDoNotAlreadyExist() {
            List<Element<Compliance>> responses = new ArrayList<>();

            List<Element<Direction>> directionWithNoCompliance = ImmutableList.of(
                Element.<Direction>builder()
                    .id(uuid)
                    .value(Direction.builder()
                        .responses(responses)
                        .build())
                    .build());

            List<Element<Direction>> directionWithCompliance = ImmutableList.of(
                Element.<Direction>builder()
                    .id(uuid)
                    .value(Direction.builder()
                        .compliance(Compliance.builder()
                            .complied("Yes")
                            .build())
                        .build())
                    .build());

            service.addResponsesToDirections(directionWithCompliance, directionWithNoCompliance);

            assertThat(directionWithNoCompliance.get(0).getValue().getResponses()).isNotEmpty();
        }

        @Test
        void shouldNotAddResponsesToDirectionWhenResponsesForDirectionAlreadyExist() {
            Compliance compliance = Compliance.builder()
                .complied("Yes")
                .build();

            List<Element<Compliance>> responses = new ArrayList<>();
            responses.add(Element.<Compliance>builder()
                .value(compliance)
                .build());

            List<Element<Direction>> directionWithNoCompliance = ImmutableList.of(
                Element.<Direction>builder()
                    .id(uuid)
                    .value(Direction.builder()
                        .directionType("Direction")
                        .assignee(LOCAL_AUTHORITY)
                        .responses(responses)
                        .build())
                    .build());

            List<Element<Direction>> directionWithCompliance = ImmutableList.of(
                Element.<Direction>builder()
                    .id(uuid)
                    .value(Direction.builder()
                        .directionType("Direction")
                        .assignee(LOCAL_AUTHORITY)
                        .compliance(compliance)
                        .build())
                    .build());

            service.addResponsesToDirections(directionWithCompliance, directionWithNoCompliance);

            assertThat(directionWithNoCompliance.get(0).getValue().getResponses()).hasSize(1);
        }

        @Test
        void shouldBeAbleToStoreReponsesToTheSameDirectionWhenDifferentPartiesAreComplying() {

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
