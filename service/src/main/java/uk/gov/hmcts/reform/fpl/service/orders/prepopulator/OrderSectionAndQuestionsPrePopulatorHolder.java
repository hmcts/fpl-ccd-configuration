package uk.gov.hmcts.reform.fpl.service.orders.prepopulator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.AllowedContactPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.AmendOrderToDownloadPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.AppointedGuardianBlockPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.ApprovalDateBlockPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.ApprovalDateTimeBlockPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.ApproverBlockPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.ChildPlacementOrderPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.CloseCaseBlockPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.DeclarationOfParentagePrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.EPOTypeAndPreventRemovalBlockPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.FamilyAssistancePrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.LinkApplicationBlockPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.LinkedToHearingBlockPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.NonMolestationOrderPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.ParentalResponsibilityPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.QuestionBlockOrderPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.RespondentsRefusedBlockPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.SingleChildSelectionBlockPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.TranslationRequirementsBlockPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.WhichChildrenBlockPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question.WhichOthersBlockPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section.ChildrenDetailsSectionPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section.DraftOrderPreviewSectionPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section.HearingDetailsSectionPrePopulator;
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
    private final LinkedToHearingBlockPrePopulator linkedToHearingBlockPrePopulator;
    private final LinkApplicationBlockPrePopulator linkApplicationBlockPrePopulator;
    private final ApprovalDateBlockPrePopulator approvalDateBlockPrePopulator;
    private final ApprovalDateTimeBlockPrePopulator approvalDateTimeBlockPrePopulator;
    private final WhichChildrenBlockPrePopulator whichChildrenBlockPrePopulator;
    private final SingleChildSelectionBlockPrePopulator singleChildSelectionBlockPrePopulator;
    private final ApproverBlockPrePopulator approverBlockPrePopulator;
    private final EPOTypeAndPreventRemovalBlockPrePopulator epoTypeAndPreventRemovalBlockPrePopulator;
    private final CloseCaseBlockPrePopulator closeCaseBlockPrePopulator;
    private final TranslationRequirementsBlockPrePopulator translationRequirementsBlockPrePopulator;
    private final AppointedGuardianBlockPrePopulator appointedGuardianBlockPrePopulator;
    private final RespondentsRefusedBlockPrePopulator respondentsRefusedBlockPrePopulator;
    private final WhichOthersBlockPrePopulator whichOthersBlockPrePopulator;
    private final AmendOrderToDownloadPrePopulator amendOrderToDownloadPrePopulator;
    private final ParentalResponsibilityPrePopulator parentalResponsibilityPrePopulator;
    private final DeclarationOfParentagePrePopulator declarationOfParentagePrePopulator;
    private final ChildPlacementOrderPrePopulator childPlacementOrderPrePopulator;
    private final AllowedContactPrePopulator allowedContactPrePopulator;
    private final FamilyAssistancePrePopulator familyAssistancePrePopulator;
    private final NonMolestationOrderPrePopulator nonMolestationOrderPrePopulator;

    // Sections
    private final HearingDetailsSectionPrePopulator hearingDetailsSectionPrePopulator;
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
            linkedToHearingBlockPrePopulator,
            linkApplicationBlockPrePopulator,
            childPlacementOrderPrePopulator,
            approvalDateBlockPrePopulator,
            approvalDateTimeBlockPrePopulator,
            whichChildrenBlockPrePopulator,
            singleChildSelectionBlockPrePopulator,
            approverBlockPrePopulator,
            epoTypeAndPreventRemovalBlockPrePopulator,
            closeCaseBlockPrePopulator,
            translationRequirementsBlockPrePopulator,
            appointedGuardianBlockPrePopulator,
            respondentsRefusedBlockPrePopulator,
            whichOthersBlockPrePopulator,
            amendOrderToDownloadPrePopulator,
            parentalResponsibilityPrePopulator,
            allowedContactPrePopulator,
            declarationOfParentagePrePopulator,
            familyAssistancePrePopulator,
            nonMolestationOrderPrePopulator
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
            draftOrderPreviewPrePopulator
        ).stream().collect(Collectors.toMap(
            OrderSectionPrePopulator::accept,
            Function.identity()
        ));

        return sectionPrePopulatorMap;
    }

}
