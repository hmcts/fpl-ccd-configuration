package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ListingActionType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class ListingActionRequest {

    List<ListingActionType> type;
    String details;
    LocalDateTime dateSent;
    LocalDateTime dateReviewed;

    @JsonIgnore
    public String toLabel() {
        return String.format("%s - %s", dateSent.format(DateTimeFormatter.ofPattern("d LLL yyyy")), getTypesLabel());
    }

    @JsonIgnore
    private String getTypesLabel() {
        return String.join(", ", type.stream().map(ListingActionType::getLabel).toList());
    }

}
