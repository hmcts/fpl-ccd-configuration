package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

@Data
@Builder(toBuilder = true)
public class GeneratedOrder {
    private final GeneratedOrderType type;
    private final String title;
    private final String details;
    private final DocumentReference document;
    private final String date;
    private final JudgeAndLegalAdvisor judgeAndLegalAdvisor;
    private final FurtherDirections furtherDirections;
    private final String expiryDate;
}
