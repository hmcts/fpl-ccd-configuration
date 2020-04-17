package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.ComplyOnBehalfEvent;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.DirectionResponse;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.ComplyOnBehalfEvent.COMPLY_ON_BEHALF_COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

/**
 * Contains methods for putting directions into a state to be complied with.
 */
@Service
public class PrepareDirectionsForUsersService {

    /**
     * <b>To be used when court is complying on behalf of other parties.</b>
     *
     * <p>Adds directions to case data for an assignee.</p>
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

                    if (eventId == COMPLY_ON_BEHALF_COURT) {
                        filterResponsesNotCompliedOnBehalfOfByTheCourt("RESPONDENT", directions);
                    } else {
                        filterResponsesNotCompliedBySolicitor(directions, PARENTS_AND_RESPONDENTS);
                    }

                    caseDetails.getData().put(assignee.toCustomDirectionField(), directions);

                    break;
                case OTHERS:
                    directions.addAll(clone);

                    if (eventId == COMPLY_ON_BEHALF_COURT) {
                        filterResponsesNotCompliedOnBehalfOfByTheCourt("OTHER", directions);
                    } else {
                        filterResponsesNotCompliedBySolicitor(directions, OTHERS);
                    }

                    caseDetails.getData().put(assignee.toCustomDirectionField(), directions);

                    break;
                case CAFCASS:
                    directions.addAll(clone);

                    List<Element<Direction>> cafcassDirections = extractPartyResponse(CAFCASS, directions);

                    caseDetails.getData().put(assignee.toCustomDirectionField(), cafcassDirections);

                    break;
                default: break;
            }
        });
    }

    private List<Element<Direction>> getClone(List<Element<Direction>> elements) {
        return elements.stream()
            .map(directionElement ->
                element(directionElement.getId(), directionElement.getValue().deepCopy()))
            .collect(toList());
    }

    /**
     * Removes responses from a direction where they have not been complied on behalf of someone by the court.
     *
     * @param onBehalfOf a string matching part of the respondingOnBehalfOf variable.
     * @param directions a list of directions.
     */
    void filterResponsesNotCompliedOnBehalfOfByTheCourt(String onBehalfOf, List<Element<Direction>> directions) {
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
    List<Element<Direction>> extractPartyResponse(DirectionAssignee assignee,
                                                  List<Element<Direction>> directions) {
        return directions.stream()
            .map(element -> element(element.getId(), element.getValue().toBuilder()
                .response(getResponse(assignee, element))
                .responses(emptyList())
                .build()))
            .collect(toList());
    }

    private DirectionResponse getResponse(DirectionAssignee assignee, Element<Direction> element) {
        return element.getValue().getResponses().stream()
            .filter(response -> response.getValue().getDirectionId().equals(element.getId()))
            .filter(response -> {
                if ("CAFCASS".equals(response.getValue().getRespondingOnBehalfOf())) {
                    return response.getValue().getRespondingOnBehalfOf().equals(assignee.toString());
                }
                return response.getValue().getAssignee().equals(assignee);
            })
            .map(Element::getValue)
            .findFirst()
            .orElse(null);
    }

    /**
     * Adds assignee directions key value pairs to caseDetails.
     *
     * <p>courtDirectionsCustom is used here to stop giving C and D permissions on the CourtDirections object
     * in draft standard direction order for gatekeeper user when they also have the court admin role.</p>
     *
     * @param assignee    the string value of the map key.
     * @param directions  a list of directions.
     * @param caseDetails the caseDetails to be updated.
     */
    public void addAssigneeDirectionKeyValuePairsToCaseData(DirectionAssignee assignee,
                                                            List<Element<Direction>> directions,
                                                            CaseDetails caseDetails) {
        List<Element<Direction>> directionsWithResponse = extractPartyResponse(assignee, directions);

        if (assignee.equals(COURT)) {
            caseDetails.getData().put(assignee.getValue().concat("Custom"), directionsWithResponse);
        } else {
            caseDetails.getData().put(assignee.getValue(), directionsWithResponse);
        }
    }
}
