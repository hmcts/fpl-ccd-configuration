package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.DirectionType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Data
@SuperBuilder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper=false)
public class GatekeepingOrderEventData extends OrderEventData {
    List<DirectionType> directionsForAllParties;
    List<DirectionType> directionsForLocalAuthority;
    List<DirectionType> directionsForRespondents;
    List<DirectionType> directionsForCafcass;
    List<DirectionType> directionsForCafcassUpdated;
    List<DirectionType> directionsForCourt;
    List<DirectionType> directionsForOthers;

    @JsonIgnore
    public List<DirectionType> getRequestedDirections() {
        return Stream.of(directionsForAllParties, directionsForLocalAuthority,
            directionsForRespondents, directionsForCafcass, directionsForCafcassUpdated,
            directionsForOthers, directionsForCourt, directionsForCourtUpdated)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .collect(toList());
    }
}
