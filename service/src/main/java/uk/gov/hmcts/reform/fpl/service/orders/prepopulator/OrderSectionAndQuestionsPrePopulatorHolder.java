package uk.gov.hmcts.reform.fpl.service.orders.prepopulator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.*;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OrderSectionAndQuestionsPrePopulatorHolder {

    // Questions
    private final LinkedToHearingBlockPrePopulator linkedToHearingBlockPrePopulator;
    private final ApprovalDateBlockPrePopulator approvalDateBlockPrePopulator;
    private final ApprovalDateTimeBlockPrePopulator approvalDateTimeBlockPrePopulator;
    private final WhichChildrenBlockPrePopulator whichChildrenBlockPrePopulator;
    private final ApproverBlockPrePopulator approverBlockPrePopulator;
    private final EPOTypeAndPreventRemovalBlockPrePopulator epoTypeAndPreventRemovalBlockPrePopulator;
    private final CloseCaseBlockPrePopulator closeCaseBlockPrePopulator;
    private final WhichOthersBlockPrePopulator whichOthersBlockPrePopulator;

    // Sections
    private final HearingDetailsSectionPrePopulator hearingDetailsSectionPrePopulator;
    private final IssuingDetailsSectionPrePopulator issuingDetailsPrePopulator;
    private final ChildrenDetailsSectionPrePopulator childrenDetailsPrePopulator;
    private final OrderDetailsSectionPrePopulator orderDetailsPrePopulator;
    private final DraftOrderPreviewSectionPrePopulator draftOrderPreviewPrePopulator;
    private final OtherDetailsSectionPrePopulator otherDetailsSectionPrePopulator;

    private Map<OrderQuestionBlock, QuestionBlockOrderPrePopulator> blockOrderPrePopulatorMap;
    private Map<OrderSection, OrderSectionPrePopulator> sectionPrePopulatorMap;

    public Map<OrderQuestionBlock, QuestionBlockOrderPrePopulator> questionBlockToPopulator() {
        if (blockOrderPrePopulatorMap != null) {
            return blockOrderPrePopulatorMap;
        }

        blockOrderPrePopulatorMap = List.of(
            linkedToHearingBlockPrePopulator,
            approvalDateBlockPrePopulator,
            approvalDateTimeBlockPrePopulator,
            whichChildrenBlockPrePopulator,
            approverBlockPrePopulator,
            epoTypeAndPreventRemovalBlockPrePopulator,
            closeCaseBlockPrePopulator,
            whichOthersBlockPrePopulator
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
            hearingDetailsSectionPrePopulator,
            issuingDetailsPrePopulator,
            childrenDetailsPrePopulator,
            orderDetailsPrePopulator,
            draftOrderPreviewPrePopulator,
            otherDetailsSectionPrePopulator
        ).stream().collect(Collectors.toMap(
            OrderSectionPrePopulator::accept,
            Function.identity()
        ));

        return sectionPrePopulatorMap;
    }

}
