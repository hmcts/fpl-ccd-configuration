package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.FinalOrderType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

@Data
@Builder(toBuilder = true)
public class FinalOrder {
    private final FinalOrderType type;
    private final String orderTitle;
    private final String orderDetails;
    private final DocumentReference document;
    private final String orderDate;
    private final JudgeAndLegalAdvisor judgeAndLegalAdvisor;
}
