package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.DirectionType;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Data
@SuperBuilder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper=true)
public class UrgentDirectionsOrderEventData extends OrderEventData {

    List<DirectionType> urgentDirectionsForAllParties;
    List<DirectionType> urgentDirectionsForLocalAuthority;
    List<DirectionType> urgentDirectionsForCafcass;


    public List<DirectionType> getRequestedDirections() {
        return Stream.of(urgentDirectionsForAllParties, urgentDirectionsForLocalAuthority,
                directionsForCafcass, urgentDirectionsForCafcass, directionsForCourt, directionsForCourtUpdated)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .collect(toList());
    }
}
