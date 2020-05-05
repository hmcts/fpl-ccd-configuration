package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Recital;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;
import uk.gov.hmcts.reform.fpl.model.interfaces.IssuableOrder;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.ORDER_ACTION;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.RECITALS;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.SCHEDULE;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.parseLocalDateFromStringUsingFormat;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseManagementOrder implements IssuableOrder {
    private final String hearingDate;
    private final UUID id;
    private final List<Element<Direction>> directions;
    private final Schedule schedule;
    private final List<Element<Recital>> recitals;
    private final CMOStatus status;
    private final String dateOfIssue;

    private OrderAction action;
    private NextHearing nextHearing;
    private DocumentReference orderDoc;

    @JsonIgnore
    public boolean isSealed() {
        return action != null && SEND_TO_ALL_PARTIES == action.getType();
    }

    @JsonIgnore
    public boolean isInJudgeReview() {
        return status == SEND_TO_JUDGE;
    }

    @JsonIgnore
    public void setOrderDocReferenceFromDocument(Document document) {
        if (document != null) {
            this.orderDoc = buildFromDocument(document);
        }
    }

    @JsonIgnore
    public void setActionWithNullDocument(OrderAction action) {
        if (action != null) {
            this.action = action.toBuilder().document(null).build();
        }
    }

    @JsonIgnore
    public void setNextHearingFromDynamicElement(HearingDateDynamicElement nextHearing) {
        if (nextHearing != null) {
            this.nextHearing = NextHearing.builder()
                .id(nextHearing.getId())
                .date(nextHearing.getDate())
                .build();
        }
    }

    @JsonIgnore
    public LocalDate getDateOfIssueAsDate() {
        return ofNullable(dateOfIssue)
            .map(date -> parseLocalDateFromStringUsingFormat(date, DATE))
            .orElse(LocalDate.now());
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
