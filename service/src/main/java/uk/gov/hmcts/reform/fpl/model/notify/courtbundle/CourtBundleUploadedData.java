package uk.gov.hmcts.reform.fpl.model.notify.courtbundle;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Data
@Builder
public class CourtBundleUploadedData implements NotifyData {
    private String caseUrl;
    private String hearingDetails;
}
