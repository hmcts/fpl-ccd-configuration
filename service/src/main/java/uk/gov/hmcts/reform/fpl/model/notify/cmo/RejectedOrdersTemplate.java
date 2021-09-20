package uk.gov.hmcts.reform.fpl.model.notify.cmo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

import java.util.List;

@Data
@Builder
public class RejectedOrdersTemplate implements NotifyData {
    private final List<String> ordersAndRequestedChanges;
    private final String subjectLineWithHearingDate;
    @JsonProperty("respondentLastName")
    private final String lastName;
    private final String caseUrl;
}
