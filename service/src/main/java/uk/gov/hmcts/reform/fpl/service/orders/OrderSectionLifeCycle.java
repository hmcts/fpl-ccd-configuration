package uk.gov.hmcts.reform.fpl.service.orders;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;

import java.util.List;

@Component
public class OrderSectionLifeCycle {
    public OrderSection calculateNextSection(OrderSection currentSection, Order order) {
        List<OrderQuestionBlock> questions = order.getQuestions();

        // there is no predefined next section for the last question so don't need to check it
        for (int i = 0; i < questions.size() - 1; i++) {
            OrderQuestionBlock question = questions.get(i);
            if (currentSection.equals(question.getSection())) {
                return questions.get(i + 1).getSection();
            }
        }

        // next section for last question is review
        return OrderSection.REVIEW;
    }
}
