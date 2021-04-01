package uk.gov.hmcts.reform.fpl.service.orders;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class OrderValidator {

    private final Map<OrderQuestionBlock, QuestionBlockOrderValidator> blockToValidator;

    public OrderValidator(QuestionBlock1OrderValidator questionBlock1OrderValidator) {
        this.blockToValidator = List.of(questionBlock1OrderValidator).stream()
            .collect(Collectors.toMap(QuestionBlockOrderValidator::accept, Function.identity()));
    }

    public List<String> validate(Order orderType, OrderSection orderSection,
                                 CaseDetails caseDetails) {

        return orderType.getQuestions().stream()
            .filter(questionBlock -> shouldValidate(orderSection, questionBlock))
            .flatMap(questionBlock -> validate(caseDetails, questionBlock))
            .collect(Collectors.toList());
    }

    private Stream<String> validate(CaseDetails caseDetails, OrderQuestionBlock questionBlock) {
        return blockToValidator.get(questionBlock).validate(caseDetails).stream();
    }

    private boolean shouldValidate(OrderSection orderSection, OrderQuestionBlock questionBlock) {
        return questionBlock.getSection().equals(orderSection) && blockToValidator.containsKey(questionBlock);
    }
}
