package uk.gov.hmcts.reform.fpl.service.orders;

import com.google.common.collect.Sets;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class OrderShowHideQuestionsCalculator {

    public Map<String, String> calculate(Order order) {
        Set<OrderQuestionBlock> questions = Sets.newHashSet(order.getQuestions());
        Set<OrderQuestionBlock> allQuestions = Stream.of(OrderQuestionBlock.values()).collect(Collectors.toSet());

        return allQuestions.stream().collect(Collectors.toMap(
            OrderQuestionBlock::getShowHideField, el -> questions.contains(el) ? "YES" : "NO"
        ));
    }

}
