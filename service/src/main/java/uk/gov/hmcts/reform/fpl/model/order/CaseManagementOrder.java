package uk.gov.hmcts.reform.fpl.model.order;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;

@Data
@Builder
public class CaseManagementOrder {
    private DocumentReference order;
    private String hearing;
    private LocalDate dateSent;
    private CMOStatus status;
    private String judgeTitleAndName;

    public static CaseManagementOrder from(DocumentReference order, HearingBooking hearing, LocalDate date) {
        return CaseManagementOrder.builder()
            .order(order)
            .hearing(hearing.toLabel(DATE))
            .dateSent(date)
            .status(SEND_TO_JUDGE)
            .judgeTitleAndName(formatJudgeTitleAndName(hearing.getJudgeAndLegalAdvisor()))
            .build();
    }
}
