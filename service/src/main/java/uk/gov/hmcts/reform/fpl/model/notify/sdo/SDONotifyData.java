package uk.gov.hmcts.reform.fpl.model.notify.sdo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

import java.util.Map;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SDONotifyData implements NotifyData {
    String leadRespondentsName;
    String respondentLastName; // only here because the ctsc was designed with this instead of leadRespondentsName
    String caseUrl;
    Map<String, Object> documentLink; // optional, only the cafcass version has this populated
    String callout;
}
