package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.DirectionResponse;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
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
    public Map<DirectionAssignee, List<Element<Direction>>> directionsToMap(CaseData caseData) {
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
    public Map<DirectionAssignee, List<Element<Direction>>> customDirectionsToMap(CaseData caseData) {
        return Map.of(
            ALL_PARTIES, defaultIfNull(caseData.getAllPartiesCustom(), emptyList()),
            LOCAL_AUTHORITY, defaultIfNull(caseData.getLocalAuthorityDirectionsCustom(), emptyList()),
            CAFCASS, defaultIfNull(caseData.getCafcassDirectionsCustom(), emptyList()),
            COURT, defaultIfNull(caseData.getCourtDirectionsCustom(), emptyList()),
            PARENTS_AND_RESPONDENTS, defaultIfNull(caseData.getRespondentDirectionsCustom(), emptyList()),
            OTHERS, defaultIfNull(caseData.getOtherPartiesDirectionsCustom(), emptyList()));
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

    /**
     * Removes directions where directionNeeded is set to No. Does not remove custom directions.
     *
     * @param directions any list of directions.
     * @return a list of directions that are marked as needed.
     */
    public List<Element<Direction>> removeUnnecessaryDirections(List<Element<Direction>> directions) {
        return directions.stream()
            .filter(this::removeDirection)
            .collect(toList());
    }

    private boolean removeDirection(Element<Direction> element) {
        return "Yes".equals(element.getValue().getDirectionNeeded()) || "Yes".equals(element.getValue().getCustom());
    }
}
