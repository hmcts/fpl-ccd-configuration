package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

/**
 * Contains methods for persisting hidden values.
 */
@Service
public class PrepareDirectionsForDataStoreService {

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
}
