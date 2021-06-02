package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.SaveOrSendGatekeepingOrder;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getJudgeForTabView;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GatekeepingOrderService {

    public SaveOrSendGatekeepingOrder buildSaveOrSendPage(CaseData caseData, Document document) {
        //add draft document
        SaveOrSendGatekeepingOrder saveOrSendGatekeepingOrder = caseData.getSaveOrSendGatekeepingOrder().toBuilder()
            .draftDocument(buildFromDocument(document))
            .orderStatus(null)
            .build();

        saveOrSendGatekeepingOrder = saveOrSendGatekeepingOrder.toBuilder()
            .nextSteps(buildNextStepsLabel(caseData))
            .build();

        return saveOrSendGatekeepingOrder;
    }

    public JudgeAndLegalAdvisor setAllocatedJudgeLabel(Judge allocatedJudge, JudgeAndLegalAdvisor issuingJudge) {
        String assignedJudgeLabel = buildAllocatedJudgeLabel(allocatedJudge);

        return issuingJudge.toBuilder().allocatedJudgeLabel(assignedJudgeLabel).build();
    }

    private boolean hasEnteredIssuingJudge(JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        if (isEmpty(judgeAndLegalAdvisor)) {
            return false;
        }

        if (judgeAndLegalAdvisor.isUsingAllocatedJudge()) {
            return true;
        } else {
            //after judge title selected, other fields are mandatory, so checking title verifies judge entry
            return isNotEmpty(judgeAndLegalAdvisor.getJudgeTitle());
        }
    }

    //this constructs a label which hides the option to seal if mandatory information is missing
    //previous button can break this functionality as logic uses a hidden field (EUI-3922)
    private String buildNextStepsLabel(CaseData caseData) {
        List<String> requiredMissingInformation = new ArrayList<>();

        if (caseData.getFirstHearing().isEmpty()) {
            requiredMissingInformation.add("* the first hearing details");
        }

        if (isEmpty(caseData.getAllocatedJudge())) {
            requiredMissingInformation.add("* the allocated judge");
        }

        if (!hasEnteredIssuingJudge(caseData.getGatekeepingOrderIssuingJudge())) {
            requiredMissingInformation.add("* the judge issuing the order");
        }

        if (requiredMissingInformation.isEmpty()) {
            return null;
        } else {
            List<String> nextStepsLabel = new ArrayList<>();
            nextStepsLabel.add("## Next steps");
            nextStepsLabel.add("Your order will be saved as a draft in 'Draft orders'.");
            nextStepsLabel.add("You cannot seal and send the order until adding:");
            nextStepsLabel.addAll(requiredMissingInformation);

            return String.join("\n\n", nextStepsLabel);
        }
    }

    public StandardDirectionOrder buildBaseGatekeepingOrder(CaseData caseData) {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor =
            getJudgeForTabView(caseData.getGatekeepingOrderIssuingJudge(), caseData.getAllocatedJudge());

        return StandardDirectionOrder.builder()
            .customDirections(caseData.getSdoDirectionCustom())
            .orderStatus(defaultIfNull(caseData.getSaveOrSendGatekeepingOrder().getOrderStatus(), DRAFT))
            .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
            .build();
    }
}
