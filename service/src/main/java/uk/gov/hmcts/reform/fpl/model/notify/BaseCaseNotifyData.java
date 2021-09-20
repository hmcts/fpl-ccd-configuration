package uk.gov.hmcts.reform.fpl.model.notify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
public class BaseCaseNotifyData implements NotifyData {
    @JsonProperty("respondentLastName")
    private final String lastName;
    private final String caseUrl;
}
