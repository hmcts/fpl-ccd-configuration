package uk.gov.hmcts.reform.fpl.model.notify.cmo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Data
@Builder
public class DraftOrdersUploadedTemplate implements NotifyData {
    private final String judgeTitle;
    private final String judgeName;
    private final String subjectLineWithHearingDate;
    @JsonProperty("respondentLastName")
    private final String lastName;
    private final String caseUrl;
    private final String draftOrders;
}
