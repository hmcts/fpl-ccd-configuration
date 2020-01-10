package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.ComplyOnBehalfEvent;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.DirectionResponse;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.ComplyOnBehalfEvent.COMPLY_ON_BEHALF_SDO;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;

@Service
public class PrepareDirectionsForUsersService {

    /**
     * <b>To be used when court is complying on behalf of other parties</b>
     * <p></p>
     * Adds directions to case data for an assignee.
     *
     * @param caseDetails   the caseDetails to add the directions to.
     * @param directionsMap a map where the DirectionAssignee key corresponds to a list of directions elements.
     */
    public void addDirectionsToCaseDetails(CaseDetails caseDetails,
                                           Map<DirectionAssignee, List<Element<Direction>>> directionsMap,
                                           ComplyOnBehalfEvent eventId) {
        directionsMap.forEach((assignee, directions) -> {
            final List<Element<Direction>> clone = getClone(directionsMap.get(ALL_PARTIES));

            switch (assignee) {
                case PARENTS_AND_RESPONDENTS:
                    directions.addAll(clone);

                    if (eventId == COMPLY_ON_BEHALF_SDO) {
                        filterResponsesNotCompliedOnBehalfOfByTheCourt("RESPONDENT", directions);
                    } else {
                        filterResponsesNotCompliedBySolicitor(directions, PARENTS_AND_RESPONDENTS);
                    }

                    caseDetails.getData().put(assignee.toCustomDirectionField(), directions);

                    break;
                case OTHERS:
                    directions.addAll(clone);

                    if (eventId == COMPLY_ON_BEHALF_SDO) {
                        filterResponsesNotCompliedOnBehalfOfByTheCourt("OTHER", directions);
                    } else {
                        filterResponsesNotCompliedBySolicitor(directions, OTHERS);
                    }

                    caseDetails.getData().put(assignee.toCustomDirectionField(), directions);

                    break;
                case CAFCASS:
                    directions.addAll(clone);

                    List<Element<Direction>> cafcassDirections = extractPartyResponse(COURT, directions);

                    caseDetails.getData().put(assignee.toCustomDirectionField(), cafcassDirections);

                    break;
            }
        });
    }

    private List<Element<Direction>> getClone(List<Element<Direction>> elements) {
        return elements.stream()
            .map(directionElement ->
                ElementUtils.element(directionElement.getId(), directionElement.getValue().deepCopy()))
            .collect(toList());
    }

    /**
     * Removes responses from a direction where they have not been complied on behalf of someone by the court.
     *
     * @param onBehalfOf a string matching part of the respondingOnBehalfOf variable.
     * @param directions a list of directions.
     */
    public void filterResponsesNotCompliedOnBehalfOfByTheCourt(String onBehalfOf, List<Element<Direction>> directions) {
        directions.forEach(directionElement -> directionElement.getValue().getResponses()
            .removeIf(element -> notCompliedWithByCourt(onBehalfOf, element)));
    }

    private void filterResponsesNotCompliedBySolicitor(List<Element<Direction>> directions,
                                                       DirectionAssignee assignee) {
        directions.forEach(directionElement -> directionElement.getValue().getResponses()
            .removeIf(element -> assignee != element.getValue().getAssignee()
                || isEmpty(element.getValue().getResponder())));
    }

    private boolean notCompliedWithByCourt(String onBehalfOf, Element<DirectionResponse> element) {
        return COURT != element.getValue().getAssignee() || isEmpty(element.getValue().getRespondingOnBehalfOf())
            || !element.getValue().getRespondingOnBehalfOf().contains(onBehalfOf);
    }

    /**
     * Extracts a specific response to a direction by a party.
     *
     * @param assignee   the role that responded to the direction.
     * @param directions a list of directions.
     * @return a list of directions with the correct response associated.
     */
    public List<Element<Direction>> extractPartyResponse(DirectionAssignee assignee,
                                                         List<Element<Direction>> directions) {
        return directions.stream()
            .map(element -> ElementUtils.element(element.getId(), element.getValue().toBuilder()
                .response(element.getValue().getResponses().stream()
                    .filter(response -> response.getValue().getDirectionId().equals(element.getId()))
                    .filter(response -> response.getValue().getAssignee().equals(assignee))
                    .map(Element::getValue)
                    .findFirst()
                    .orElse(null))
                .responses(emptyList())
                .build()))
            .collect(toList());
    }

    /**
     * Adds assignee directions key value pairs to caseDetails.
     *
     * <p></p>
     * courtDirectionsCustom is used here to stop giving C and D permissions on the CourtDirections object
     * in draft standard direction order for gatekeeper user when they also have the court admin role.
     *
     * @param assignee    the string value of the map key.
     * @param directions  a list of directions.
     * @param caseDetails the caseDetails to be updated.
     */
    public void addAssigneeDirectionKeyValuePairsToCaseData(DirectionAssignee assignee,
                                                            List<Element<Direction>> directions,
                                                            CaseDetails caseDetails) {
        if (assignee.equals(COURT)) {
            caseDetails.getData().put(assignee.getValue().concat("Custom"), extractPartyResponse(assignee, directions));
        } else {
            caseDetails.getData().put(assignee.getValue(), extractPartyResponse(assignee, directions));
        }
    }
}
