package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.types.ComplexType;
import uk.gov.hmcts.ccd.sdk.types.ComplexType;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.interfaces.IssuableOrder;

import java.util.List;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;

@Getter
@ComplexType(name = "StandardDirectionOrder")
public class Order extends OrderForHearing implements IssuableOrder {
    private final OrderStatus orderStatus;
    private final JudgeAndLegalAdvisor judgeAndLegalAdvisor;

    @Builder
    public Order(String hearingDate,
                 String dateOfIssue,
                 List<Element<Direction>> directions,
                 DocumentReference orderDoc,
                 OrderStatus orderStatus,
                 JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        super(hearingDate, dateOfIssue, directions, orderDoc);
        this.orderStatus = orderStatus;
        this.judgeAndLegalAdvisor = judgeAndLegalAdvisor;
    }

    @JsonIgnore
    public boolean isSealed() {
        return SEALED == orderStatus;
    }

    @JsonIgnore
    public void setDirectionsToEmptyList() {
        this.directions = emptyList();
    }
}
