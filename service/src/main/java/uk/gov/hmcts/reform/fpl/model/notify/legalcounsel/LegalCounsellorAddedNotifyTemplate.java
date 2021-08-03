package uk.gov.hmcts.reform.fpl.model.notify.legalcounsel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.SharedNotifyTemplate;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder
public class LegalCounsellorAddedNotifyTemplate extends SharedNotifyTemplate {
    @JsonProperty("caseID")
    private final String caseId;
}
