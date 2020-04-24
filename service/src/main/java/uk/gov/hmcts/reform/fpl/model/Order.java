package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.interfaces.IssuableOrder;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Order implements IssuableOrder {
    private final String hearingDate;
    private final List<Element<Direction>> directions;
    private final OrderStatus orderStatus;
    private final DocumentReference orderDoc;
    private final JudgeAndLegalAdvisor judgeAndLegalAdvisor;
    private final String dateOfIssue;

    @JsonIgnore
    public boolean isSealed() {
        return SEALED == orderStatus;
    }

    @JsonIgnore
    public boolean isDraft() {
        return !isSealed();
    }
}
