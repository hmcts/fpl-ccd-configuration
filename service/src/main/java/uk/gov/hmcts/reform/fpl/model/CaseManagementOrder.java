package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Recital;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.interfaces.IssuableOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.ORDER_ACTION;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.RECITALS;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.SCHEDULE;

@Getter
public class CaseManagementOrder extends OrderForHearing implements IssuableOrder {
    private final UUID id;
    private final Schedule schedule;
    private final List<Element<Recital>> recitals;
    private final CMOStatus status;

    private OrderAction action;
    private NextHearing nextHearing;

    @Builder(toBuilder = true)
    public CaseManagementOrder(String hearingDate,
                               String dateOfIssue,
                               List<Element<Direction>> directions,
                               DocumentReference orderDoc,
                               UUID id,
                               Schedule schedule,
                               List<Element<Recital>> recitals,
                               CMOStatus status,
                               OrderAction action,
                               NextHearing nextHearing) {
        super(hearingDate, dateOfIssue, directions, orderDoc);
        this.id = id;
        this.schedule = schedule;
        this.recitals = recitals;
        this.status = status;
        this.action = action;
        this.nextHearing = nextHearing;
    }

    @JsonIgnore
    public boolean isSealed() {
        return action != null && SEND_TO_ALL_PARTIES == action.getType();
    }

    @JsonIgnore
    public boolean isInJudgeReview() {
        return status == SEND_TO_JUDGE;
    }

    @JsonIgnore
    public void setActionWithNullDocument(OrderAction action) {
        if (action != null) {
            this.action = action.toBuilder().document(null).build();
        }
    }

    @JsonIgnore
    public void setNextHearingFromDynamicElement(DynamicListElement nextHearing) {
        if (nextHearing != null) {
            this.nextHearing = NextHearing.builder()
                .id(nextHearing.getCode())
                .date(nextHearing.getLabel())
                .build();
        }
    }

    @JsonIgnore
    public Map<String, Object> getCCDFields() {
        Map<String, Object> data = new HashMap<>();

        if (schedule != null) {
            data.put(SCHEDULE.getKey(), schedule);
        }

        if (recitals != null) {
            data.put(RECITALS.getKey(), recitals);
        }

        if (action != null) {
            data.put(ORDER_ACTION.getKey(), action);
        }

        return data;
    }
}
