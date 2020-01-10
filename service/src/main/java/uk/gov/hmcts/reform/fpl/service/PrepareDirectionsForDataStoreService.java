package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.ComplyOnBehalfEvent;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.DirectionResponse;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.ComplyOnBehalfEvent.COMPLY_ON_BEHALF_SDO;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;

@Service
public class PrepareDirectionsForDataStoreService {
    private final UserDetailsService userDetailsService;
    private final DirectionHelperService directionService;

    public PrepareDirectionsForDataStoreService(UserDetailsService userDetailsService,
                                                DirectionHelperService directionService) {
        this.userDetailsService = userDetailsService;
        this.directionService = directionService;
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
        directionToAddValues.forEach(directionToAddValue -> directionWithValues
            .stream()
            .filter(direction -> hasSameDirectionType(directionToAddValue, direction))
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

    private boolean hasSameDirectionType(Element<Direction> directionToAddValue, Element<Direction> direction) {
        return direction.getValue().getDirectionType().equals(directionToAddValue.getValue().getDirectionType());
    }

    /**
     * Adds a response by an assignee to a direction where the direction ID of the response matches the direction ID
     * of the direction.
     *
     * @param responses  a list of single responses to directions.
     * @param directions a list of directions with combined responses.
     */
    public void addResponsesToDirections(List<DirectionResponse> responses, List<Element<Direction>> directions) {
        directions.forEach(direction -> responses.stream()
            .filter(response -> response.getDirectionId().equals(direction.getId()))
            .forEach(response -> {
                direction.getValue().getResponses().removeIf(element -> responseExists(response, element));
                direction.getValue().getResponses().add(ElementUtils.element(response));
            }));
    }

    private boolean responseExists(DirectionResponse response, Element<DirectionResponse> element) {
        return isNotEmpty(element.getValue()) && element.getValue().getAssignee().equals(response.getAssignee())
            && respondingOnBehalfIsEqual(response, element);
    }

    private boolean respondingOnBehalfIsEqual(DirectionResponse response, Element<DirectionResponse> element) {
        return defaultIfNull(element.getValue().getRespondingOnBehalfOf(), "")
            .equals(defaultIfNull(response.getRespondingOnBehalfOf(), ""));
    }

    /**
     * Adds responses to directions in standard direction order {@link uk.gov.hmcts.reform.fpl.model.Order}.
     *
     * @param caseData caseData containing custom role collections and standard directions order.
     */
    public void addComplyOnBehalfResponsesToDirectionsInOrder(CaseData caseData,
                                                              ComplyOnBehalfEvent eventId,
                                                              String authorisation) {
        Map<DirectionAssignee, List<Element<Direction>>> customDirectionsMap =
            directionService.collectCustomDirectionsToMap(caseData);

        List<Element<Direction>> directionsToComplyWith = directionService.getDirectionsToComplyWith(caseData);

        customDirectionsMap.forEach((assignee, directions) -> {
            switch (assignee) {
                case CAFCASS:
                    List<DirectionResponse> responses = getResponses(ImmutableMap.of(COURT, directions)).stream()
                        .map(response -> response.toBuilder().respondingOnBehalfOf("CAFCASS").build())
                        .collect(toList());

                    addResponsesToDirections(responses, directionsToComplyWith);

                    break;

                case PARENTS_AND_RESPONDENTS:
                    List<Element<DirectionResponse>> respondentResponses = addValuesToListResponses(
                        directions, eventId, authorisation, PARENTS_AND_RESPONDENTS);

                    addResponseElementsToDirections(respondentResponses, directionsToComplyWith);

                    break;
                case OTHERS:
                    List<Element<DirectionResponse>> otherResponses = addValuesToListResponses(
                        directions, eventId, authorisation, OTHERS);

                    addResponseElementsToDirections(otherResponses, directionsToComplyWith);

                    break;
            }
        });
    }

    //TODO: refactor of addCourtAssigneeAndDirectionId would remove dependency on eventId.
    private List<Element<DirectionResponse>> addValuesToListResponses(List<Element<Direction>> directions,
                                                                      ComplyOnBehalfEvent eventId,
                                                                      String authorisation,
                                                                      DirectionAssignee assignee) {
        final UUID[] id = new UUID[1];

        return directions.stream()
            .map(directionElement -> {
                id[0] = directionElement.getId();

                return directionElement.getValue().getResponses();
            })
            .flatMap(List::stream)
            .map(element -> getDirectionResponse(eventId, authorisation, id[0], element, assignee))
            .collect(toList());
    }

    private Element<DirectionResponse> getDirectionResponse(ComplyOnBehalfEvent event,
                                                            String authorisation,
                                                            UUID id,
                                                            Element<DirectionResponse> response,
                                                            DirectionAssignee assignee) {
        if (event == COMPLY_ON_BEHALF_SDO) {
            return addCourtAssigneeAndDirectionId(id, response);
        } else {
            return addResponderAssigneeAndDirectionId(response, authorisation, assignee, id);
        }
    }

    //TODO: if name of user complying for court is added then this can be merged with logic from method below.
    private Element<DirectionResponse> addCourtAssigneeAndDirectionId(UUID id, Element<DirectionResponse> element) {
        return ElementUtils.element(element.getId(), element.getValue().toBuilder()
            .assignee(COURT)
            .directionId(id)
            .build());
    }

    private Element<DirectionResponse> addResponderAssigneeAndDirectionId(Element<DirectionResponse> response,
                                                                          String authorisation,
                                                                          DirectionAssignee assignee,
                                                                          UUID id) {
        return ElementUtils.element(response.getId(), response.getValue().toBuilder()
            .assignee(assignee)
            .responder(getUsername(response, authorisation))
            .directionId(id)
            .build());
    }

    private String getUsername(Element<DirectionResponse> element, String authorisation) {
        if (isEmpty(element.getValue().getResponder())) {
            return userDetailsService.getUserName(authorisation);
        } else {
            return element.getValue().getResponder();
        }
    }

    /**
     * Adds a direction response element to a direction where the direction id matches.
     *
     * @param responses  a list of direction response elements.
     * @param directions a list of directions.
     */
    public void addResponseElementsToDirections(List<Element<DirectionResponse>> responses,
                                                List<Element<Direction>> directions) {
        directions.forEach(direction -> responses.stream()
            .filter(response -> response.getValue().getDirectionId().equals(direction.getId()))
            .forEach(response -> {
                direction.getValue().getResponses().removeIf(x -> response.getId().equals(x.getId()));
                direction.getValue().getResponses().add(response);
            }));
    }

    /**
     * Gets responses with populated directionId and assignee wherever a response is present within a direction.
     *
     * @param directionMap a map of directions where assignee is key and value is a list of directions.
     * @return a list of responses with hidden variables added.
     */
    public List<DirectionResponse> getResponses(Map<DirectionAssignee, List<Element<Direction>>> directionMap) {
        return directionMap.entrySet()
            .stream()
            .map(entry -> addHiddenVariablesToResponseForAssignee(entry.getKey(), entry.getValue()))
            .flatMap(List::stream)
            .map(element -> element.getValue().getResponse())
            .collect(toList());
    }

    private List<Element<Direction>> addHiddenVariablesToResponseForAssignee(DirectionAssignee assignee,
                                                                             List<Element<Direction>> directions) {
        return directions.stream()
            .filter(elementsWithInvalidResponse())
            .map(element -> ElementUtils.element(element.getId(), element.getValue().toBuilder()
                .response(element.getValue().getResponse().toBuilder()
                    .assignee(assignee)
                    .directionId(element.getId())
                    .build())
                .build()))
            .collect(toList());
    }

    private Predicate<Element<Direction>> elementsWithInvalidResponse() {
        return element -> isNotEmpty(element.getValue().getResponse())
            && isNotEmpty(element.getValue().getResponse().getComplied());
    }
}
