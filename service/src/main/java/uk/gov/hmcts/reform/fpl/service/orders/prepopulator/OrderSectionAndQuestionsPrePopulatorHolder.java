package uk.gov.hmcts.reform.fpl.service.orders.prepopulator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.ApproverBlockPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.EPOAddressPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.QuestionBlockOrderPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.WhichChildrenBlockPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section.ChildrenDetailsSectionPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section.DraftOrderPreviewSectionPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section.IssuingDetailsSectionPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section.OrderDetailsSectionPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section.OrderSectionPrePopulator;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OrderSectionAndQuestionsPrePopulatorHolder {

    // Questions
    private final WhichChildrenBlockPrePopulator whichChildrenBlockPrePopulator;
    private final ApproverBlockPrePopulator approverBlockPrePopulator;
    private final EPOAddressPrePopulator epoAddressPrePopulator;

    // Sections
    private final IssuingDetailsSectionPrePopulator issuingDetailsPrePopulator;
    private final ChildrenDetailsSectionPrePopulator childrenDetailsPrePopulator;
    private final OrderDetailsSectionPrePopulator orderDetailsPrePopulator;
    private final DraftOrderPreviewSectionPrePopulator draftOrderPreviewPrePopulator;

    private Map<OrderQuestionBlock, QuestionBlockOrderPrePopulator> blockOrderPrePopulatorMap;
    private Map<OrderSection, OrderSectionPrePopulator> sectionPrePopulatorMap;

    public Map<OrderQuestionBlock, QuestionBlockOrderPrePopulator> questionBlockToPopulator() {
        if (blockOrderPrePopulatorMap != null) {
            return blockOrderPrePopulatorMap;
        }

        blockOrderPrePopulatorMap = List.of(
            whichChildrenBlockPrePopulator,
            approverBlockPrePopulator,
            epoAddressPrePopulator
        ).stream().collect(Collectors.toMap(
            QuestionBlockOrderPrePopulator::accept,
            Function.identity()
        ));

        return blockOrderPrePopulatorMap;
    }

    public Map<OrderSection, OrderSectionPrePopulator> sectionBlockToPopulator() {
        if (sectionPrePopulatorMap != null) {
            return sectionPrePopulatorMap;
        }

        sectionPrePopulatorMap = List.of(
            issuingDetailsPrePopulator,
            childrenDetailsPrePopulator,
            orderDetailsPrePopulator,
            draftOrderPreviewPrePopulator
        ).stream().collect(Collectors.toMap(
            OrderSectionPrePopulator::accept,
            Function.identity()
        ));

        return sectionPrePopulatorMap;
    }

}
