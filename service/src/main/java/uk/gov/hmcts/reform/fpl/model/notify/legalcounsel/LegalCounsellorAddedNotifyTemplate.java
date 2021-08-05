package uk.gov.hmcts.reform.fpl.model.notify.legalcounsel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Value
@Builder
public class LegalCounsellorAddedNotifyTemplate implements NotifyData {
    @JsonProperty("caseID")
    String caseId;
    String childLastName;
    String caseUrl;
}
