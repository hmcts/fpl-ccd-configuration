package uk.gov.hmcts.reform.fpl.service.orders;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OrderPrePopulator {

    private final Map<OrderQuestionBlock, QuestionBlockOrderPrePopulator> blockToValidator;

    public OrderPrePopulator(WhichChildrenBlockPrePopulator whichChildrenBlockPrePopulator,
        ApproverBlockPrePopulator approverBlockPrePopulator) {
        this.blockToValidator = List.of(
            whichChildrenBlockPrePopulator,
            approverBlockPrePopulator
        ).stream().collect(Collectors.toMap(
            QuestionBlockOrderPrePopulator::accept,
            Function.identity()
        ));
    }

    public Map<String, Object> prePopulate(Order orderType, OrderSection orderSection,
                                           CaseData caseData, CaseDetails caseDetails) {

        return orderType.getQuestions().stream()
            .filter(questionBlock ->
                questionBlock.getSection().equals(orderSection) && blockToValidator.containsKey(questionBlock))
            .flatMap(questionBlock -> blockToValidator.get(questionBlock)
                .prePopulate(caseData, caseDetails)
                .entrySet()
                .stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
