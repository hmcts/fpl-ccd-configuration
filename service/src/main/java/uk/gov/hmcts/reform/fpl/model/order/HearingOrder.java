package uk.gov.hmcts.reform.fpl.model.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;

import java.time.LocalDate;
import java.util.List;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;

@Data
@Builder(toBuilder = true)
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class HearingOrder implements RemovableOrder, AmendableOrder {
    private String title;
    private HearingOrderType type;
    private DocumentReference order;
    private DocumentReference lastUploadedOrder;
    private String hearing;
    // Case management order, 21 June 2020
    private LocalDate dateSent;
    private LocalDate dateIssued;
    private final LocalDate amendedDate;
    private CMOStatus status;
    private String judgeTitleAndName;
    private String requestedChanges;
    private List<Element<SupportingEvidenceBundle>> supportingDocs;
    private String removalReason;

    public static HearingOrder from(DocumentReference order, HearingBooking hearing, LocalDate date) {
        return from(order, hearing, date, AGREED_CMO, null);
    }

    public static HearingOrder from(DocumentReference order, HearingBooking hearing, LocalDate date,
                                    HearingOrderType orderType,
                                    List<Element<SupportingEvidenceBundle>> supportingDocs) {
        return HearingOrder.builder()
            .type(orderType)
            .title(orderType == AGREED_CMO ? "Agreed CMO discussed at hearing" : "Draft CMO from advocates' meeting")
            .order(order)
            .hearing(hearing.toLabel())
            .dateSent(date)
            .status(orderType == AGREED_CMO ? SEND_TO_JUDGE : DRAFT)
            .judgeTitleAndName(formatJudgeTitleAndName(hearing.getJudgeAndLegalAdvisor()))
            .supportingDocs(supportingDocs)
            .build();
    }

    @JsonIgnore
    public boolean isRemovable() {
        return true;
    }

    @Override
    public String asLabel() {
        if (type == C21) {
            return format("Draft order sent on %s", formatLocalDateToString(dateSent, DATE));
        } else {
            if (APPROVED.equals(status)) {
                return format("Sealed case management order issued on %s",
                    formatLocalDateToString(dateIssued, DATE));
            }

            if (SEND_TO_JUDGE.equals(status)) {
                return format("Agreed case management order sent on %s",
                    formatLocalDateToString(dateSent, DATE));
            }

            return format("Draft case management order sent on %s",
                formatLocalDateToString(dateSent, DATE));
        }
    }

    @Override
    public LocalDate amendableSortDate() {
        return dateIssued;
    }

    @JsonIgnore
    @Override
    public DocumentReference getDocument() {
        return order;
    }
}
