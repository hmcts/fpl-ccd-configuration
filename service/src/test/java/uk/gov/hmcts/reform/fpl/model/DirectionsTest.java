package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
class DirectionsTest {

    @Test
    void shouldGetListOfDirectionsWithPopulatedCCDFieldsFromIndividualDirectionFields() {
        CaseData caseData = CaseData.builder()
            .directionsForCaseManagementOrder(Directions.builder()
                .allPartiesCustomCMO(wrapElements(Direction.builder().build()))
                .localAuthorityDirectionsCustomCMO(wrapElements(Direction.builder().build()))
                .respondentDirectionsCustomCMO(wrapElements(Direction.builder().build()))
                .cafcassDirectionsCustomCMO(wrapElements(Direction.builder().build()))
                .otherPartiesDirectionsCustomCMO(wrapElements(Direction.builder().build()))
                .courtDirectionsCustomCMO(wrapElements(Direction.builder().build()))
                .build())
            .build();

        List<Element<Direction>> expected = wrapElements(getDirection(ALL_PARTIES), getDirection(LOCAL_AUTHORITY),
            getDirection(PARENTS_AND_RESPONDENTS), getDirection(CAFCASS), getDirection(OTHERS), getDirection(COURT));

        assertThat(caseData.getDirectionsForCaseManagementOrder().getDirectionsList()).isEqualTo(expected);
    }

    private Direction getDirection(DirectionAssignee assignee) {
        return Direction.builder()
            .assignee(assignee)
            .custom("Yes")
            .readOnly("No")
            .build();
    }
}
