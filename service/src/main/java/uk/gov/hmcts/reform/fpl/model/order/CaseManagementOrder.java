package uk.gov.hmcts.reform.fpl.model.order;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;

@Data
@Builder
public class CaseManagementOrder {
    private DocumentReference order;
    private String hearing;
    private LocalDate dateSent;
    private CMOStatus status;

    public static CaseManagementOrder createDraft(DocumentReference order, HearingBooking hearing, LocalDate date) {
        return CaseManagementOrder.builder()
            .order(order)
            .hearing(hearing.toLabel(DATE))
            .dateSent(date)
            .status(SEND_TO_JUDGE)
            .build();
    }
}
