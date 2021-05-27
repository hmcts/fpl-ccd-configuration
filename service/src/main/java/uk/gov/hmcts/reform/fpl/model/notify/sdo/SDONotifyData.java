package uk.gov.hmcts.reform.fpl.model.notify.sdo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

import java.util.Map;

@Value
@Builder
public class SDONotifyData implements NotifyData {
    @JsonProperty("leadRespondentsName")
    String lastName;
    String caseUrl; // optional, not in the cafcass version due to it having the below field instead
    Map<String, Object> documentLink; // optional, only the cafcass version has this populated
    String callout;
}
