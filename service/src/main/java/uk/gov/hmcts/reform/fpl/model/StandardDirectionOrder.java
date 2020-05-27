package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.interfaces.IssuableOrder;

import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;

@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class StandardDirectionOrder extends OrderForHearing implements IssuableOrder {
    private final OrderStatus orderStatus;
    private final JudgeAndLegalAdvisor judgeAndLegalAdvisor;

    @JsonIgnore
    public boolean isSealed() {
        return SEALED == orderStatus;
    }
}
