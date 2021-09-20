package uk.gov.hmcts.reform.fpl.model.notify.cmo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Data
@Builder
public class CMOReadyToSealTemplate implements NotifyData {
    private String judgeTitle;
    private String judgeName;
    private String subjectLineWithHearingDate;
    @JsonProperty("respondentLastName")
    private String lastName;
    private String caseUrl;
}
