package uk.gov.hmcts.reform.fpl.model.notify.returnedcase;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

import java.util.Map;

@Data
@Builder
public class ReturnedCaseTemplate implements NotifyData {
    private String familyManCaseNumber;
    private String returnedReasons;
    private String returnedNote;
    @JsonProperty("respondentLastName")
    private String lastName;
    private String respondentFullName;
    private String caseUrl;
    private String localAuthority;
    //No strict type as this is defined by uk.gov.service.notify.NotificationClient
    private Map<String, Object> applicationDocumentUrl;
}
