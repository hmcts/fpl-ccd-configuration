package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.CMOActionType;
import uk.gov.hmcts.reform.fpl.enums.CMONextHearingType;


@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class CaseManagementOrderAction {
    private final CMOActionType cmoActionType;
    private final CMONextHearingType cmoNextHearingType;
    private final String judgeRequestedChange;
}
