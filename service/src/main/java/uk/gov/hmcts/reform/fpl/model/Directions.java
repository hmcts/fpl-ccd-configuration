package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;

import static java.util.Comparator.comparingInt;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.service.CommonDirectionService.assignCustomDirections;

@Data
@Builder
@AllArgsConstructor
public class Directions {
    private final List<Element<Direction>> allPartiesCustomCMO;
    private final List<Element<Direction>> localAuthorityDirectionsCustomCMO;
    private final List<Element<Direction>> respondentDirectionsCustomCMO;
    private final List<Element<Direction>> cafcassDirectionsCustomCMO;
    private final List<Element<Direction>> otherPartiesDirectionsCustomCMO;
    private final List<Element<Direction>> courtDirectionsCustomCMO;

    List<Element<Direction>> getDirectionsList() {
        List<Element<Direction>> directions = new ArrayList<>();

        directions.addAll(getAllPartiesCustomCMO());
        directions.addAll(getLocalAuthorityDirectionsCustomCMO());
        directions.addAll(getRespondentDirectionsCustomCMO());
        directions.addAll(getCafcassDirectionsCustomCMO());
        directions.addAll(getOtherPartiesDirectionsCustomCMO());
        directions.addAll(getCourtDirectionsCustomCMO());

        return directions;
    }

    private List<Element<Direction>> getAllPartiesCustomCMO() {
        return assignCustomDirections(allPartiesCustomCMO, ALL_PARTIES);
    }

    private List<Element<Direction>> getLocalAuthorityDirectionsCustomCMO() {
        return assignCustomDirections(localAuthorityDirectionsCustomCMO, LOCAL_AUTHORITY);
    }

    private List<Element<Direction>> getRespondentDirectionsCustomCMO() {
        return orderByRespondent(assignCustomDirections(respondentDirectionsCustomCMO, PARENTS_AND_RESPONDENTS));
    }

    private List<Element<Direction>> getCafcassDirectionsCustomCMO() {
        return assignCustomDirections(cafcassDirectionsCustomCMO, CAFCASS);
    }

    private List<Element<Direction>> getOtherPartiesDirectionsCustomCMO() {
        return orderByOther(assignCustomDirections(otherPartiesDirectionsCustomCMO, OTHERS));
    }

    private List<Element<Direction>> getCourtDirectionsCustomCMO() {
        return assignCustomDirections(courtDirectionsCustomCMO, COURT);
    }

    private List<Element<Direction>> orderByRespondent(List<Element<Direction>> directions) {
        directions.sort(comparingInt(direction -> direction.getValue()
            .getParentsAndRespondentsAssignee()
            .ordinal()));

        return directions;
    }

    private List<Element<Direction>> orderByOther(List<Element<Direction>> directions) {
        directions.sort(comparingInt(direction -> direction.getValue()
            .getOtherPartiesAssignee()
            .ordinal()));

        return directions;
    }
}
