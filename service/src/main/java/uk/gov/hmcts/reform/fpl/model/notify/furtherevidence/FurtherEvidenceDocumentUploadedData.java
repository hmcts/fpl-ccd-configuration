package uk.gov.hmcts.reform.fpl.model.notify.furtherevidence;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Data
@Builder
public class FurtherEvidenceDocumentUploadedData implements NotifyData {
    private String caseUrl;
    private String respondentLastName;
    private String userName;
    private String callout;
}
