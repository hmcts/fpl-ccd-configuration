package uk.gov.hmcts.reform.fpl.model.notify;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class SharedLocalAuthorityChangedNotifyData implements NotifyData {

    private String caseName;
    private String ccdNumber;
    private String childLastName;
    private String secondaryLocalAuthority;
    private String designatedLocalAuthority;
}
