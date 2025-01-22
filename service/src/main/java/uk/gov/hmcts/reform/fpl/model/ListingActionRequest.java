package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ListingActionType;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.format.DateTimeFormatter.ofPattern;

@Value
@Builder(toBuilder = true)
public class ListingActionRequest {

    List<ListingActionType> type;
    String details;
    LocalDateTime dateSent;
    LocalDateTime dateReviewed;

    @JsonIgnore
    public String toLabel() {
        return String.format("%s - Sent %s", getTypesLabel(), dateSent.format(ofPattern("d LLL yyyy")));
    }

    @JsonIgnore
    public String getTypesLabel() {
        return String.join(", ", type.stream().map(ListingActionType::getLabel).toList());
    }

}
