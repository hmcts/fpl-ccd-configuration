package uk.gov.hmcts.reform.fpl.model.notify;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CaseTransferredNotifyData implements NotifyData {

    private String caseName;
    private String caseUrl;
    private String ccdNumber;
    private String childLastName;
    private String newDesignatedLocalAuthority;
    private String prevDesignatedLocalAuthority;
}
