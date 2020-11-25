package uk.gov.hmcts.reform.fpl.service;

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
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

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
    void removeUnnecessaryDirections_shouldRemoveDirectionsWhenDirectionsAreMarkedAsNotNeeded() {
        List<Direction> directions = unwrapElements(service.removeUnnecessaryDirections(directionsMarkedAsRemoved()));

        List<Direction> expectedDirections = unwrapElements(Stream.of(buildDirections(ALL_PARTIES, "Yes"),
            buildDirections(CAFCASS, "Yes"),
            buildDirections(COURT, "Yes"))
            .flatMap(Collection::stream)
            .collect(toList()));

        assertThat(directions).isEqualTo(expectedDirections);
    }

    @Test
    void removeUnnecessaryDirections_shouldNotRemoveCustomDirectionsWhenCustomDirectionsPresent() {
        List<Direction> directions = unwrapElements(service.removeUnnecessaryDirections(buildCustomDirections()));

        List<Direction> expectedDirections = unwrapElements(buildCustomDirections());

        assertThat(directions).isEqualTo(expectedDirections);
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
                element(Direction.builder()
                    .custom(custom)
                    .build())));

            assertThat(filteredDirections).hasSize(1);
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
        return Lists.newArrayList(element(Direction.builder()
            .directionType("direction")
            .directionText("example direction text")
            .assignee(assignee)
            .build()));
    }

    private List<Element<Direction>> buildDirections(DirectionAssignee assignee, String directionNeeded) {
        return Lists.newArrayList(element(Direction.builder()
            .directionType("direction")
            .directionText("example direction text")
            .directionNeeded(directionNeeded)
            .assignee(assignee)
            .build()));
    }

    private List<Element<Direction>> buildDirections(DirectionAssignee assignee, UUID directionId) {
        return Lists.newArrayList(element(directionId, Direction.builder()
            .directionType("direction")
            .directionText("example direction text")
            .assignee(assignee)
            .build()));
    }

    private List<Element<Direction>> buildCustomDirections() {
        return Lists.newArrayList(element(
            Direction.builder()
                .directionType("direction")
                .directionText("example direction text")
                .custom("Yes")
                .build()));
    }

    private List<Element<Direction>> directionsMarkedAsRemoved() {
        return Stream.of(buildDirections(ALL_PARTIES, "Yes"),
            buildDirections(LOCAL_AUTHORITY, "No"),
            buildDirections(PARENTS_AND_RESPONDENTS, "No"),
            buildDirections(CAFCASS, "Yes"),
            buildDirections(OTHERS, (String) null), //TO replicate CMO behaviour
            buildDirections(COURT, "Yes"))
            .flatMap(Collection::stream)
            .collect(toList());
    }
}
