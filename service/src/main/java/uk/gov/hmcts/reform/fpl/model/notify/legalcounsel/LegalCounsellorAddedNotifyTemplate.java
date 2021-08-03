package uk.gov.hmcts.reform.fpl.model.notify.legalcounsel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@EqualsAndHashCode
@ToString(callSuper = true)
@Data
@SuperBuilder
public class LegalCounsellorAddedNotifyTemplate implements NotifyData {
    @JsonProperty("caseID")
    private final String caseId;
    private final String childLastName;
    private final String caseUrl;
}
