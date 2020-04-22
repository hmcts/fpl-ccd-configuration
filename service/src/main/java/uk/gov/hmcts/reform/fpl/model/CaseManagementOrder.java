package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Recital;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;
import uk.gov.hmcts.reform.fpl.model.interfaces.IssuableOrder;

import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseManagementOrder implements IssuableOrder {
    private final DocumentReference orderDoc;
    private final String hearingDate;
    private final UUID id;
    private final List<Element<Direction>> directions;
    private final Schedule schedule;
    private final List<Element<Recital>> recitals;
    private final CMOStatus status;
    private final OrderAction action;
    private final NextHearing nextHearing;
    private final String dateOfIssue;

    public boolean isDraft() {
        return action == null || !SEND_TO_ALL_PARTIES.equals(action.getType());
    }

    public boolean isSealed() {
        return !isDraft();
    }

    public boolean isInJudgeReview() {
        return status == SEND_TO_JUDGE;
    }
}
