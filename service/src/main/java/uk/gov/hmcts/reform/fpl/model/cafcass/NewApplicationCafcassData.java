package uk.gov.hmcts.reform.fpl.model.cafcass;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewApplicationCafcassData implements CafcassData {
    private String localAuthourity;
    private String ordersAndDirections;
    private boolean timeFramePresent;
    private String timeFrameValue;
    private String firstRespondentName;
    private String eldestChildLastName;
}
