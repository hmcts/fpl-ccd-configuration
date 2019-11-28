package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.CMOActionStatus;
import uk.gov.hmcts.reform.fpl.enums.CMONextHearingStatus;


@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionCaseManagementOrder {
    private final CMOActionStatus cmoActionStatus;
    private final CMONextHearingStatus cmoNextHearingStatus;
}
