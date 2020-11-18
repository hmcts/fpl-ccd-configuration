package uk.gov.hmcts.reform.fpl.model.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;

@Data
@Builder(toBuilder = true)
public class CaseManagementOrder implements RemovableOrder {
    private DocumentReference order;
    private String hearing;
    private LocalDate dateSent;
    private LocalDate dateIssued;
    private CMOStatus status;
    private String judgeTitleAndName;
    private String requestedChanges;
    private List<Element<SupportingEvidenceBundle>> supportingDocs;
    private String removalReason;

    public static CaseManagementOrder from(DocumentReference order, HearingBooking hearing, LocalDate date) {
        return from(order, hearing, date, SEND_TO_JUDGE, null);
    }

    public static CaseManagementOrder from(DocumentReference order, HearingBooking hearing, LocalDate date,
                                           CMOStatus status, List<Element<SupportingEvidenceBundle>> supportingDocs) {
        return CaseManagementOrder.builder()
            .order(order)
            .hearing(hearing.toLabel())
            .dateSent(date)
            .status(status)
            .judgeTitleAndName(formatJudgeTitleAndName(hearing.getJudgeAndLegalAdvisor()))
            .supportingDocs(supportingDocs)
            .build();
    }

    public boolean isRemovable() {
        return APPROVED.equals(status);
    }

    public String asLabel() {
        return getType() + " - " + formatLocalDateToString(dateIssued, "d MMMM yyyy");
    }

    @JsonIgnore
    public String getType() {
        return "Case management order";
    }
}
