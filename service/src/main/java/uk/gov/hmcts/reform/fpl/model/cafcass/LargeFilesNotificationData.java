package uk.gov.hmcts.reform.fpl.model.cafcass;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LargeFilesNotificationData implements CafcassData {
    private String familyManCaseNumber;
    private String documentName;
    private String caseUrl;
    private String notificationType;
}
