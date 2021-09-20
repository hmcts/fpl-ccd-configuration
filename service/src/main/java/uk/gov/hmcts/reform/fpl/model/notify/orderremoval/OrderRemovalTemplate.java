package uk.gov.hmcts.reform.fpl.model.notify.orderremoval;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Builder
@Data
public class OrderRemovalTemplate implements NotifyData {
    private String caseReference;
    private String caseUrl;
    @JsonProperty("respondentLastName")
    private String lastName;
    private String removalReason;
}
