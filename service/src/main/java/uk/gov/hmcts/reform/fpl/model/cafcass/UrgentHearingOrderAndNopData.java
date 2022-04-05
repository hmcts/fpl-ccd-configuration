package uk.gov.hmcts.reform.fpl.model.cafcass;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UrgentHearingOrderAndNopData implements CafcassData {
    private String leadRespondentsName;
    private String callout;

    public boolean isUrgent() {
        return true;
    }
}
