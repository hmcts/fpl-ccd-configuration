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

import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseManagementOrder implements IssuableOrder {
    private DocumentReference orderDoc;
    private final String hearingDate;
    private final UUID id;
    private final List<Element<Direction>> directions;
    private final Schedule schedule;
    private final List<Element<Recital>> recitals;
    private final CMOStatus status;
    private OrderAction action;
    private NextHearing nextHearing;
    private final String dateOfIssue;

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
}
