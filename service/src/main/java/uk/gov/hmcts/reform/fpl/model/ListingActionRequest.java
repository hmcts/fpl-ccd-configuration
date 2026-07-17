package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ListingActionType;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.format.DateTimeFormatter.ofPattern;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Value
@Builder(toBuilder = true)
public class ListingActionRequest {

    @CCD(label = "Type")
    List<ListingActionType> type;
    @CCD(label = "Details", typeOverride = FieldType.TextArea)
    String details;
    @CCD(label = "Date sent")
    LocalDateTime dateSent;
    @CCD(label = "Date reviewed")
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
