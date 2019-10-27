package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

@Data
@Builder(toBuilder = true)
// Temp c21 Order
public class C21Order {
    private final String orderTitle;
    private final String orderDetails;
    private final DocumentReference c21OrderDocument;
    private final JudgeAndLegalAdvisor judgeAndLegalAdvisor;
}
