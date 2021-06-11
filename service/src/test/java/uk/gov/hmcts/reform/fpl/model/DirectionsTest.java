package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.OtherPartiesDirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.ParentsAndRespondentsDirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.OtherPartiesDirectionAssignee.OTHER_1;
import static uk.gov.hmcts.reform.fpl.enums.OtherPartiesDirectionAssignee.OTHER_3;
import static uk.gov.hmcts.reform.fpl.enums.OtherPartiesDirectionAssignee.OTHER_5;
import static uk.gov.hmcts.reform.fpl.enums.ParentsAndRespondentsDirectionAssignee.RESPONDENT_1;
import static uk.gov.hmcts.reform.fpl.enums.ParentsAndRespondentsDirectionAssignee.RESPONDENT_3;
import static uk.gov.hmcts.reform.fpl.enums.ParentsAndRespondentsDirectionAssignee.RESPONDENT_5;
import static uk.gov.hmcts.reform.fpl.model.Directions.getAssigneeToDirectionMapping;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
class DirectionsTest {

    @Nested
    class SortDirectionsByAssignee {

        @Test
        void shouldSortDirectionsIntoSeparateEntriesInMapWhenManyAssignees() {
            List<Element<Direction>> directions = wrapElements(getDirection(LOCAL_AUTHORITY), getDirection(COURT));

            Map<DirectionAssignee, List<Element<Direction>>> mapping = getAssigneeToDirectionMapping(directions);

            assertKeyContainsCorrectDirection(LOCAL_AUTHORITY, mapping);
            assertKeyContainsCorrectDirection(COURT, mapping);

            Stream.of(DirectionAssignee.values())
                .filter(assignee -> assignee != LOCAL_AUTHORITY && assignee != COURT)
                .forEach(assignee -> assertThat(mapping).containsEntry(assignee, emptyList()));
        }

        @Test
        void shouldAddEmptyListValueWhenKeyNotPresentInMap() {
            Map<DirectionAssignee, List<Element<Direction>>> mapping = getAssigneeToDirectionMapping(emptyList());

            Stream.of(DirectionAssignee.values())
                .forEach(assignee -> assertThat(mapping).containsEntry(assignee, emptyList()));
        }

        private void assertKeyContainsCorrectDirection(DirectionAssignee court,
                                                       Map<DirectionAssignee, List<Element<Direction>>> mapping) {
            assertThat(unwrapElements(mapping.get(court))).containsOnly(getDirection(court));
        }
    }

    @Test
    void shouldGetListOfDirectionsWithPopulatedCCDFieldsFromIndividualDirectionFields() {
        Directions directions = Directions.builder()
            .allPartiesCustomCMO(wrapElements(Direction.builder().build()))
            .localAuthorityDirectionsCustomCMO(wrapElements(Direction.builder().build()))
            .respondentDirectionsCustomCMO(wrapElements(Direction.builder().build()))
            .cafcassDirectionsCustomCMO(wrapElements(Direction.builder().build()))
            .otherPartiesDirectionsCustomCMO(wrapElements(Direction.builder().build()))
            .courtDirectionsCustomCMO(wrapElements(Direction.builder().build()))
            .build();

        List<Element<Direction>> expected = wrapElements(getDirection(ALL_PARTIES), getDirection(LOCAL_AUTHORITY),
            getDirection(PARENTS_AND_RESPONDENTS), getDirection(CAFCASS), getDirection(OTHERS), getDirection(COURT));

        assertThat(directions.getDirectionsList()).isEqualTo(expected);
        assertTrue(directions.containsDirections());
    }

    @Test
    void shouldReturnEmptyListWhenNoDirections() {
        Directions directions = Directions.builder().build();

        assertThat(directions.getDirectionsList()).isEmpty();
        assertFalse(directions.containsDirections());
    }

    @Test
    void shouldOrderDirectionsByOtherWhenManyOtherAssignees() {
        Direction first = directionFor(OTHER_1);
        Direction third = directionFor(OTHER_3);
        Direction fifth = directionFor(OTHER_5);

        Directions directions = Directions.builder()
            .otherPartiesDirectionsCustomCMO(wrapElements(fifth, first, third))
            .build();

        assertThat(unwrapElements(directions.getDirectionsList()))
            .containsExactly(getDirection(OTHER_1), getDirection(OTHER_3), getDirection(OTHER_5));
    }

    @Test
    void shouldOrderDirectionsByRespondentWhenManyRespondentAssignees() {
        Direction first = directionFor(RESPONDENT_1);
        Direction third = directionFor(RESPONDENT_3);
        Direction fifth = directionFor(RESPONDENT_5);

        Directions directions = Directions.builder()
            .respondentDirectionsCustomCMO(wrapElements(fifth, first, third))
            .build();

        assertThat(unwrapElements(directions.getDirectionsList()))
            .containsExactly(getDirection(RESPONDENT_1), getDirection(RESPONDENT_3), getDirection(RESPONDENT_5));
    }

    private Direction directionFor(OtherPartiesDirectionAssignee assignee) {
        return Direction.builder().otherPartiesAssignee(assignee).build();
    }

    private Direction directionFor(ParentsAndRespondentsDirectionAssignee assignee) {
        return Direction.builder().parentsAndRespondentsAssignee(assignee).build();
    }

    private Direction getDirection(DirectionAssignee assignee) {
        return Direction.builder()
            .assignee(assignee)
            .custom("Yes")
            .readOnly("No")
            .build();
    }

    private Direction getDirection(OtherPartiesDirectionAssignee other) {
        return Direction.builder()
            .assignee(OTHERS)
            .custom("Yes")
            .readOnly("No")
            .otherPartiesAssignee(other)
            .build();
    }

    private Direction getDirection(ParentsAndRespondentsDirectionAssignee respondent) {
        return Direction.builder()
            .assignee(PARENTS_AND_RESPONDENTS)
            .custom("Yes")
            .readOnly("No")
            .parentsAndRespondentsAssignee(respondent)
            .build();
    }
}
