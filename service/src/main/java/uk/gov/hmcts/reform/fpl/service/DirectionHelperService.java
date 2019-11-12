package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;

/**
 * A service that helps with the sorting and editing of directions.
 */
@Service
public class DirectionHelperService {

    /**
     * Combines role directions into a single List of directions.
     *
     * @param caseData data containing all the directions by role.
     * @return directions.
     **/
    public List<Element<Direction>> combineAllDirections(CaseData caseData) {
        List<Element<Direction>> directions = new ArrayList<>();

        directions.addAll(caseData.getAllParties());

        directions.addAll(assignCustomDirections(caseData.getAllPartiesCustom(), ALL_PARTIES));

        directions.addAll(caseData.getLocalAuthorityDirections());

        directions.addAll(assignCustomDirections(caseData.getLocalAuthorityDirectionsCustom(), LOCAL_AUTHORITY));

        directions.addAll(caseData.getParentsAndRespondentsDirections());

        directions.addAll(assignCustomDirections(caseData.getParentsAndRespondentsCustom(), PARENTS_AND_RESPONDENTS));

        directions.addAll(caseData.getCafcassDirections());

        directions.addAll(assignCustomDirections(caseData.getCafcassDirectionsCustom(), CAFCASS));

        directions.addAll(caseData.getOtherPartiesDirections());

        directions.addAll(assignCustomDirections(caseData.getOtherPartiesDirectionsCustom(), OTHERS));

        directions.addAll(caseData.getCourtDirections());

        directions.addAll(assignCustomDirections(caseData.getCourtDirectionsCustom(), COURT));

        return directions;
    }

    /**
     * Adds values that would otherwise be lost in CCD to directions.
     * Values include readOnly, directionRemovable and directionText.
     *
     * @param directionWithValues  an order object that should be generated using original case data.
     * @param directionToAddValues an order object that should be generated using case data edited through a ccd event.
     */
    public void persistHiddenDirectionValues(List<Element<Direction>> directionWithValues,
                                             List<Element<Direction>> directionToAddValues) {
        directionToAddValues
            .forEach(directionToAddValue -> directionWithValues
                .stream()
                .filter(direction ->
                    direction.getValue().getDirectionType().equals(directionToAddValue.getValue().getDirectionType()))
                .forEach(direction -> {
                    directionToAddValue.getValue().setReadOnly(direction.getValue().getReadOnly());
                    directionToAddValue.getValue().setDirectionRemovable(direction.getValue().getDirectionRemovable());
                    directionToAddValue.getValue().setAssignee(defaultIfNull(
                        directionToAddValue.getValue().getAssignee(), direction.getValue().getAssignee()));

                    if (!direction.getValue().getReadOnly().equals("No")) {
                        directionToAddValue.getValue().setDirectionText(direction.getValue().getDirectionText());
                    }
                }));
    }

    /**
     * Splits a list of directions into a map where the key is the role of the direction assignee and the value is the
     * list of directions belonging to the role.
     *
     * @param directions a list of directions with various assignees.
     * @return Map of role name, list of directions.
     */
    public Map<String, List<Element<Direction>>> sortDirectionsByAssignee(List<Element<Direction>> directions) {
        return directions.stream()
            .filter(x -> x.getValue().getCustom() == null)
            .collect(groupingBy(directionElement -> directionElement.getValue().getAssignee().getValue()));
    }

    /**
     * Iterates over a list of directions and adds numbers to the directionType starting from 2.
     *
     * @param directions a list of directions.
     * @return a list of directions with numbered directionType.
     */
    public List<Element<Direction>> numberDirections(List<Element<Direction>> directions) {
        AtomicInteger at = new AtomicInteger(2);

        return directions.stream()
            .map(direction -> {
                Direction.DirectionBuilder directionBuilder = direction.getValue().toBuilder();

                return Element.<Direction>builder()
                    .id(direction.getId())
                    .value(directionBuilder
                        .directionType(at.getAndIncrement() + ". " + direction.getValue().getDirectionType())
                        .build())
                    .build();
            })
            .collect(toList());
    }

    /**
     * Takes a direction from a configuration file and builds a CCD direction.
     *
     * @param direction  the direction taken from json config.
     * @param completeBy the date to be completed by. Can be null.
     * @return Direction to be stored in CCD.
     */
    public Element<Direction> constructDirectionForCCD(DirectionConfiguration direction, LocalDateTime completeBy) {
        return Element.<Direction>builder()
            .id(randomUUID())
            .value(Direction.builder()
                .directionType(direction.getTitle())
                .directionText(direction.getText())
                .assignee(direction.getAssignee())
                .directionRemovable(booleanToYesOrNo(direction.getDisplay().isDirectionRemovable()))
                .readOnly(booleanToYesOrNo(direction.getDisplay().isShowDateOnly()))
                .dateToBeCompletedBy(completeBy)
                .build())
            .build();
    }

    private String booleanToYesOrNo(boolean value) {
        return value ? "Yes" : "No";
    }

    private List<Element<Direction>> assignCustomDirections(List<Element<Direction>> directions,
                                                            DirectionAssignee assignee) {
        if (!isNull(directions)) {
            return directions.stream()
                .map(element -> Element.<Direction>builder()
                    .id(element.getId())
                    .value(element.getValue().toBuilder()
                        .assignee(assignee)
                        .custom("Yes")
                        .readOnly("No")
                        .build())
                    .build())
                .collect(toList());
        } else {
            return emptyList();
        }
    }
}
