package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.CMOActionType;
import uk.gov.hmcts.reform.fpl.enums.CMONextHearingType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.UUID;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseManagementOrderAction {
    private final DocumentReference orderDoc;
    private final CMOActionType cmoActionType;
    private final CMONextHearingType cmoNextHearingType;
    private final String changeRequestedByJudge;
    private final UUID id;
    private final String nextHearingDate;
}
