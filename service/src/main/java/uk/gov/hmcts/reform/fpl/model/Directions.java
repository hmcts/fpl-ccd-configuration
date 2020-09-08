package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparingInt;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

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

    @JsonIgnore
    public List<Element<Direction>> getDirectionsList() {
        List<Element<Direction>> directions = new ArrayList<>();

        ofNullable(getAllPartiesCustomCMO()).ifPresent(directions::addAll);
        ofNullable(getLocalAuthorityDirectionsCustomCMO()).ifPresent(directions::addAll);
        ofNullable(getRespondentDirectionsCustomCMO()).ifPresent(list -> directions.addAll(orderByRespondent(list)));
        ofNullable(getCafcassDirectionsCustomCMO()).ifPresent(directions::addAll);
        ofNullable(getOtherPartiesDirectionsCustomCMO()).ifPresent(list -> directions.addAll(orderByOther(list)));
        ofNullable(getCourtDirectionsCustomCMO()).ifPresent(directions::addAll);

        return directions;
    }

    @JsonIgnore
    public static Map<DirectionAssignee, List<Element<Direction>>> getAssigneeToDirectionMapping(
        List<Element<Direction>> directions) {
        Map<DirectionAssignee, List<Element<Direction>>> map = directions.stream()
            .collect(groupingBy(element -> element.getValue().getAssignee()));

        stream(DirectionAssignee.values()).forEach(assignee -> map.putIfAbsent(assignee, new ArrayList<>()));

        return map;
    }

    boolean containsDirections() {
        return allPartiesCustomCMO != null || localAuthorityDirectionsCustomCMO != null
            || respondentDirectionsCustomCMO != null || cafcassDirectionsCustomCMO != null
            || otherPartiesDirectionsCustomCMO != null || courtDirectionsCustomCMO != null;
    }

    // getters need to be public for correct Json serialisation
    public List<Element<Direction>> getAllPartiesCustomCMO() {
        return assignDirections(allPartiesCustomCMO, ALL_PARTIES);
    }

    public List<Element<Direction>> getLocalAuthorityDirectionsCustomCMO() {
        return assignDirections(localAuthorityDirectionsCustomCMO, LOCAL_AUTHORITY);
    }

    public List<Element<Direction>> getRespondentDirectionsCustomCMO() {
        return assignDirections(respondentDirectionsCustomCMO, PARENTS_AND_RESPONDENTS);
    }

    public List<Element<Direction>> getCafcassDirectionsCustomCMO() {
        return assignDirections(cafcassDirectionsCustomCMO, CAFCASS);
    }

    public List<Element<Direction>> getOtherPartiesDirectionsCustomCMO() {
        return assignDirections(otherPartiesDirectionsCustomCMO, OTHERS);
    }

    public List<Element<Direction>> getCourtDirectionsCustomCMO() {
        return assignDirections(courtDirectionsCustomCMO, COURT);
    }

    private List<Element<Direction>> assignDirections(List<Element<Direction>> directions, DirectionAssignee assignee) {
        return ofNullable(directions).map(values -> addAssignee(values, assignee)).orElse(emptyList());
    }

    private List<Element<Direction>> addAssignee(List<Element<Direction>> directions, DirectionAssignee assignee) {
        return directions.stream()
            .map(element -> element(element.getId(), element.getValue().toBuilder()
                .assignee(assignee)
                .custom("Yes")
                .readOnly("No")
                .build()))
            .collect(toList());
    }

    private List<Element<Direction>> orderByRespondent(List<Element<Direction>> directions) {
        directions.sort(comparingInt(direction -> direction.getValue().getParentsAndRespondentsAssignee().ordinal()));

        return directions;
    }

    private List<Element<Direction>> orderByOther(List<Element<Direction>> directions) {
        directions.sort(comparingInt(direction -> direction.getValue().getOtherPartiesAssignee().ordinal()));

        return directions;
    }
}
