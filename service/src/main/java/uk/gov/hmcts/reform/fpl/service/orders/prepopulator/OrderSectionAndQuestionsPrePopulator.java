package uk.gov.hmcts.reform.fpl.service.orders.prepopulator;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OrderSectionAndQuestionsPrePopulator {

    private final OrderSectionAndQuestionsPrePopulatorHolder holder;

    public Map<String, Object> prePopulate(Order orderType, OrderSection orderSection, CaseData caseData) {

        Map<String, Object> populatedFieldsForGivenSection = Optional.ofNullable(holder.sectionBlockToPopulator())
            .map(m -> m.get(orderSection))
            .map(p -> Maps.newHashMap(p.prePopulate(caseData)))
            .orElse(Maps.newHashMap());

        Map<String, Object> populatedFieldsForQuestionBlocksForGivenSection = orderType.getQuestionsBlocks().stream()
            .filter(questionBlock -> questionBlock.getSection().equals(orderSection))
            .map(questionBlock -> holder.questionBlockToPopulator().get(questionBlock))
            .filter(Objects::nonNull)
            .flatMap(questionBlockOrderPrePopulator -> questionBlockOrderPrePopulator.prePopulate(caseData)
                .entrySet()
                .stream()
            )
            .filter(entry -> entry.getValue() != null)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        populatedFieldsForGivenSection.putAll(populatedFieldsForQuestionBlocksForGivenSection);

        return populatedFieldsForGivenSection;
    }

}
