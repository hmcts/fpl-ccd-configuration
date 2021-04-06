package uk.gov.hmcts.reform.fpl.service.orders;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OrderValidator {

    private final OrderValidatorHolder holder;

    public List<String> validate(Order orderType, OrderSection orderSection,
                                 CaseDetails caseDetails) {

        return orderType.getQuestions().stream()
            .filter(questionBlock -> shouldValidate(orderSection, questionBlock))
            .flatMap(questionBlock -> validate(caseDetails, questionBlock).stream())
            .collect(Collectors.toList());
    }

    private List<String> validate(CaseDetails caseDetails, OrderQuestionBlock questionBlock) {
        return holder.blockToValidator().get(questionBlock).validate(caseDetails);
    }

    private boolean shouldValidate(OrderSection orderSection, OrderQuestionBlock questionBlock) {
        return questionBlock.getSection().equals(orderSection) && holder.blockToValidator().containsKey(questionBlock);
    }
}
