package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Order {
    private final String hearingDate;
    private final List<Element<Direction>> directions;
    private final OrderStatus orderStatus;
    private final DocumentReference orderDoc;
    private final JudgeAndLegalAdvisor judgeAndLegalAdvisor;
    private final String dateOfIssue;
}
