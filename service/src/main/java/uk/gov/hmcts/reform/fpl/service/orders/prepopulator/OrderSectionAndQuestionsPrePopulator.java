package uk.gov.hmcts.reform.fpl.service.orders.prepopulator;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})

public class OrderSectionAndQuestionsPrePopulator {

    private final OrderSectionAndQuestionsPrePopulatorHolder holder;

    public Map<String, Object> prePopulate(Order orderType, OrderSection orderSection, CaseData caseData) {

        Map<String, Object> items = Optional.ofNullable(holder.sectionBlockToPopulator().getOrDefault(orderSection,
            null)).map(p -> Maps.newHashMap(p.prePopulate(caseData))
        ).orElse(Maps.newHashMap());

        items.putAll(orderType.getQuestions().stream()
            .filter(questionBlock ->
                questionBlock.getSection().equals(orderSection)
                    && holder.questionBlockToPopulator().containsKey(questionBlock))
            .flatMap(questionBlock -> holder.questionBlockToPopulator().get(questionBlock)
                .prePopulate(caseData)
                .entrySet()
                .stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        return items;
    }
}
