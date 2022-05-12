package uk.gov.hmcts.reform.fpl.model.cafcass;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NoticeOfHearingCafcassData implements CafcassData {
    private String firstRespondentName;
    private String eldestChildLastName;
    private String hearingType;
    private String hearingDate;
    private String hearingVenue;
    private String preHearingTime;
    private String hearingTime;
}