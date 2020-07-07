package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.ActionType;
import uk.gov.hmcts.reform.fpl.enums.NextHearingType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;


/**
 * Action decided by the Judge for the CMO.
 *
 * @deprecated to be removed with {@link uk.gov.hmcts.reform.fpl.model.CaseManagementOrder}
 */
@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Deprecated
public class OrderAction {
    //document needs to be here due to CCD UI not allowing for complex types to be split over multiple screens.
    private final DocumentReference document;
    private final ActionType type;
    private final NextHearingType nextHearingType;
    private final String changeRequestedByJudge;

    @JsonIgnore
    public boolean isSendToAllPartiesType() {
        return SEND_TO_ALL_PARTIES == this.type;
    }
}
