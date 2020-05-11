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

    public List<Element<Direction>> getAllPartiesCustomCMO() {
        return assignCustomDirections(allPartiesCustomCMO, ALL_PARTIES);
    }

    public List<Element<Direction>> getLocalAuthorityDirectionsCustomCMO() {
        return assignCustomDirections(localAuthorityDirectionsCustomCMO, LOCAL_AUTHORITY);
    }

    public List<Element<Direction>> getRespondentDirectionsCustomCMO() {
        return orderByRespondent(assignCustomDirections(respondentDirectionsCustomCMO, PARENTS_AND_RESPONDENTS));
    }

    public List<Element<Direction>> getCafcassDirectionsCustomCMO() {
        return assignCustomDirections(cafcassDirectionsCustomCMO, CAFCASS);
    }

    public List<Element<Direction>> getOtherPartiesDirectionsCustomCMO() {
        return orderByOther(assignCustomDirections(otherPartiesDirectionsCustomCMO, OTHERS));
    }

    public List<Element<Direction>> getCourtDirectionsCustomCMO() {
        return assignCustomDirections(courtDirectionsCustomCMO, COURT);
    }

    List<Element<Direction>> getAllDirections() {
        List<Element<Direction>> directions = new ArrayList<>();

        directions.addAll(getAllPartiesCustomCMO());
        directions.addAll(getLocalAuthorityDirectionsCustomCMO());
        directions.addAll(getRespondentDirectionsCustomCMO());
        directions.addAll(getCafcassDirectionsCustomCMO());
        directions.addAll(getOtherPartiesDirectionsCustomCMO());
        directions.addAll(getCourtDirectionsCustomCMO());

        return directions;
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
