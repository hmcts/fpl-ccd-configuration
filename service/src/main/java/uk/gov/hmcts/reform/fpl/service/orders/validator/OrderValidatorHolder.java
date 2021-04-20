package uk.gov.hmcts.reform.fpl.service.orders.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OrderValidatorHolder {

    private final ApprovalDateValidator approvalDateValidator;
    private final WhichChildrenValidator whichChildrenValidator;
    private final EPORemovalAddressValidator epoRemovalAddressValidator;
    private final EPOEndDateValidator epoEndDateValidator;

    private Map<OrderQuestionBlock, QuestionBlockOrderValidator> blockToValidator;

    public Map<OrderQuestionBlock, QuestionBlockOrderValidator> blockToValidator() {
        if (blockToValidator != null) {
            return blockToValidator;
        }
        blockToValidator = List.of(
            whichChildrenValidator,
            approvalDateValidator,
            epoRemovalAddressValidator,
            epoEndDateValidator
        ).stream().collect(Collectors.toMap(QuestionBlockOrderValidator::accept, Function.identity()));

        return blockToValidator;
    }
}
