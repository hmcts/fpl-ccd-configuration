package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.DirectionResponse;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

/**
 * A service that helps with the sorting and editing of directions.
 */
@Service
public class CommonDirectionService {

    /**
     * Combines role directions into a single List of directions.
     *
     * @param caseData data containing all the directions by role.
     * @return directions.
     **/
    public List<Element<Direction>> combineAllDirections(CaseData caseData) {
        return
            Stream.of(caseData.getAllParties(),
                getElements(caseData.getAllPartiesCustom(), ALL_PARTIES),
                caseData.getLocalAuthorityDirections(),
                getElements(caseData.getLocalAuthorityDirectionsCustom(), LOCAL_AUTHORITY),
                caseData.getRespondentDirections(),
                getElements(caseData.getRespondentDirectionsCustom(), PARENTS_AND_RESPONDENTS),
                caseData.getCafcassDirections(),
                getElements(caseData.getCafcassDirectionsCustom(), CAFCASS),
                caseData.getOtherPartiesDirections(),
                getElements(caseData.getOtherPartiesDirectionsCustom(), OTHERS),
                caseData.getCourtDirections(),
                getElements(caseData.getCourtDirectionsCustom(), COURT))
                .flatMap(Collection::stream)
                .collect(toList());
    }

    /**
     * Iterates over a list of directions and sets properties assignee, custom and readOnly.
     *
     * @param directions a list of directions.
     * @param assignee   the assignee of the directions to be returned.
     * @return A list of custom directions.
     */
    List<Element<Direction>> assignCustomDirections(List<Element<Direction>> directions,
                                                    DirectionAssignee assignee) {
        return getElements(directions, assignee);
    }

    private List<Element<Direction>> getElements(List<Element<Direction>> directions, DirectionAssignee assignee) {
        return ofNullable(directions)
            .map(values -> values.stream()
                .map(element -> element(element.getId(), element.getValue().toBuilder()
                    .assignee(assignee)
                    .custom("Yes")
                    .readOnly("No")
                    .build()))
                .collect(toList()))
            .orElseGet(Collections::emptyList);
    }

    /**
     * Collects directions for all parties and places them into single map, where the key is the role direction,
     * and the value is the list of directions associated.
     *
     * <p>courtDirectionsCustom is used due to a CCD permissions workaround.</p>
     *
     * @param caseData data from case.
     * @return Map of roles and directions.
     */
    public Map<DirectionAssignee, List<Element<Direction>>> collectDirectionsToMap(CaseData caseData) {
        return Map.of(
            ALL_PARTIES, defaultIfNull(caseData.getAllParties(), emptyList()),
            LOCAL_AUTHORITY, defaultIfNull(caseData.getLocalAuthorityDirections(), emptyList()),
            CAFCASS, defaultIfNull(caseData.getCafcassDirections(), emptyList()),
            COURT, defaultIfNull(caseData.getCourtDirectionsCustom(), emptyList()),
            PARENTS_AND_RESPONDENTS, defaultIfNull(caseData.getRespondentDirections(), emptyList()),
            OTHERS, defaultIfNull(caseData.getOtherPartiesDirections(), emptyList()));
    }

    /**
     * Collects custom directions for all parties and places them into single map, where the key is the role direction,
     * and the value is the list of directions associated.
     *
     * @param caseData data from case.
     * @return Map of roles and directions.
     */
    Map<DirectionAssignee, List<Element<Direction>>> collectCustomDirectionsToMap(CaseData caseData) {
        return Map.of(
            ALL_PARTIES, defaultIfNull(caseData.getAllPartiesCustom(), emptyList()),
            LOCAL_AUTHORITY, defaultIfNull(caseData.getLocalAuthorityDirectionsCustom(), emptyList()),
            CAFCASS, defaultIfNull(caseData.getCafcassDirectionsCustom(), emptyList()),
            COURT, defaultIfNull(caseData.getCourtDirectionsCustom(), emptyList()),
            PARENTS_AND_RESPONDENTS, defaultIfNull(caseData.getRespondentDirectionsCustom(), emptyList()),
            OTHERS, defaultIfNull(caseData.getOtherPartiesDirectionsCustom(), emptyList()));
    }

    /**
     * Splits a list of directions into a map where the key is the role of the direction assignee and the value is the
     * list of directions belonging to the role.
     *
     * @param directions a list of directions with various assignees.
     * @return Map of role name, list of directions.
     */
    public Map<DirectionAssignee, List<Element<Direction>>> sortDirectionsByAssignee(
        List<Element<Direction>> directions) {
        return directions.stream()
            .collect(groupingBy(directionElement -> directionElement.getValue().getAssignee()));
    }

    /**
     * Adds any {@link DirectionAssignee} not present to a map with an empty list of directions.
     *
     * @param map assignee, directions key value pairs.
     */
    public void addEmptyDirectionsForAssigneeNotInMap(Map<DirectionAssignee, List<Element<Direction>>> map) {
        stream(DirectionAssignee.values()).forEach(assignee -> map.putIfAbsent(assignee, new ArrayList<>()));
    }

    /**
     * Removes directions where custom is set to Yes.
     *
     * @param directions any list of directions.
     * @return a list of directions that are not custom.
     */
    public List<Element<Direction>> removeCustomDirections(List<Element<Direction>> directions) {
        return directions.stream()
            .filter(element -> !"Yes".equals(element.getValue().getCustom()))
            .collect(toList());
    }

    /**
     * Filters a list of directions to return only the directions belonging to specific assignee.
     *
     * @param directions a list of directions.
     * @param assignee   the assignee of the directions to be returned.
     * @return a list of directions belonging to the assignee.
     */
    public List<Element<Direction>> getDirectionsForAssignee(List<Element<Direction>> directions,
                                                             DirectionAssignee assignee) {
        return directions.stream()
            .filter(element -> element.getValue().getAssignee().equals(assignee))
            .collect(toList());
    }

    //TODO: numbering done for SDO in formatTitle method. CMO to do the same? FPLA-1481
    /**
     * Iterates over a list of directions and adds numbers to the directionType starting from 2.
     *
     * @param directions a list of directions.
     * @return a list of directions with numbered directionType.
     */
    public List<Element<Direction>> numberDirections(List<Element<Direction>> directions) {
        AtomicInteger at = new AtomicInteger(2);

        return directions.stream()
            //FIXME FPLA-1481 can deal with that but as this is only used by CMO the line below doesn't make sense
            .map(direction -> element(direction.getId(), direction.getValue().toBuilder()
                .directionType(at.getAndIncrement() + ". " + direction.getValue().getDirectionType())
                .build()))
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
        return element(Direction.builder()
            .directionType(direction.getTitle())
            .directionText(direction.getText())
            .assignee(direction.getAssignee())
            .directionRemovable(booleanToYesOrNo(direction.getDisplay().isDirectionRemovable()))
            .readOnly(booleanToYesOrNo(direction.getDisplay().isShowDateOnly()))
            .dateToBeCompletedBy(completeBy)
            .build());
    }

    /**
     * Returns a list of directions to comply with.
     *
     * @param caseData case data with standard directions and case management order directions.
     * @return most recent directions that need to be complied with.
     */
    public List<Element<Direction>> getDirectionsToComplyWith(CaseData caseData) {
        if (caseData.getServedCaseManagementOrders().isEmpty()) {
            return caseData.getStandardDirectionOrder().getDirections();
        }
        return caseData.getServedCaseManagementOrders().get(0).getValue().getDirections();
    }

    /**
     * Gets all single responses from a map of directions and their assignee.
     *
     * @param map a map of directions where assignee is key and value is a list of directions.
     * @return a list of responses.
     */
    public List<DirectionResponse> getResponses(Map<DirectionAssignee, List<Element<Direction>>> map) {
        return map.values()
            .stream()
            .map(elements -> elements.stream()
                .filter(element -> element.getValue().isCompliedWith())
                .map(element -> element.getValue().getResponse())
                .collect(toList()))
            .flatMap(List::stream)
            .collect(toList());
    }

    private String booleanToYesOrNo(boolean value) {
        return value ? "Yes" : "No";
    }
}
