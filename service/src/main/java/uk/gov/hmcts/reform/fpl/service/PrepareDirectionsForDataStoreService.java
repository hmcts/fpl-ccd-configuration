package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.DirectionResponse;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

/**
 * Contains methods for persisting hidden values and adding responses to directions.
 */
@Service
public class PrepareDirectionsForDataStoreService {
    private final CommonDirectionService directionService;
    private final IdamClient idamClient;
    private final RequestData requestData;

    public PrepareDirectionsForDataStoreService(IdamClient idamClient,
                                                CommonDirectionService directionService,
                                                RequestData requestData) {
        this.idamClient = idamClient;
        this.directionService = directionService;
        this.requestData = requestData;
    }

    /**
     * Adds values that would otherwise be lost in CCD to directions.
     * Values include readOnly, directionRemovable and directionText.
     *
     * @param directionsWithValues  an order object that should be generated using original case data.
     * @param directionsToAddValues an order object that should be generated using case data edited through a ccd event.
     */
    public void persistHiddenDirectionValues(List<Element<Direction>> directionsWithValues,
                                             List<Element<Direction>> directionsToAddValues) {
        directionsToAddValues.forEach(elementToAddValue ->
            directionsWithValues.stream()
                .filter(element -> hasSameDirectionType(elementToAddValue, element))
                .forEach(element -> {
                    Direction direction = elementToAddValue.getValue();
                    Direction value = element.getValue();

                    direction.setReadOnly(value.getReadOnly());
                    direction.setDirectionRemovable(value.getDirectionRemovable());
                    direction.setAssignee(defaultIfNull(direction.getAssignee(), value.getAssignee()));
                    direction.setDateToBeCompletedBy(
                        defaultIfNull(direction.getDateToBeCompletedBy(), value.getDateToBeCompletedBy()));

                    if (!value.getReadOnly().equals("No")) {
                        direction.setDirectionText(value.getDirectionText());
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
                direction.getValue().getResponses().add(element(response));
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
}
