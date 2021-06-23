package uk.gov.hmcts.reform.fpl.model.notify.furtherevidence;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Data
@Builder
public class FurtherEvidenceDocumentUploadedData implements NotifyData {
    private String caseUrl;
    @JsonProperty("respondentLastName")
    private String lastName;
    private String userName;
    private String callout;
}
