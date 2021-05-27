package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.SaveOrSendGatekeepingOrder;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GatekeepingOrderService {

    public SaveOrSendGatekeepingOrder buildSaveOrSendPage(CaseData caseData, Document document) {
        //generate draft document
        SaveOrSendGatekeepingOrder saveOrSendGatekeepingOrder = caseData.getSaveOrSendGatekeepingOrder().toBuilder()
            .draftDocument(buildFromDocument(document))
            .orderStatus(null)
            .build();

        //check if missing fields for seal and send
        if (caseData.getFirstHearing().isEmpty()
            || isEmpty(caseData.getAllocatedJudge())
            || !hasEnteredIssuingJudge(caseData.getGatekeepingOrderIssuingJudge())) {
            saveOrSendGatekeepingOrder = saveOrSendGatekeepingOrder.toBuilder()
                .nextSteps(buildNextStepsLabel(caseData))
                .build();
        }

        return saveOrSendGatekeepingOrder;
    }

    public JudgeAndLegalAdvisor setAllocatedJudgeLabel(Judge allocatedJudge) {
        String assignedJudgeLabel = buildAllocatedJudgeLabel(allocatedJudge);

        return JudgeAndLegalAdvisor.builder()
            .allocatedJudgeLabel(assignedJudgeLabel)
            .build();
    }

    private boolean hasEnteredIssuingJudge(JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        if (judgeAndLegalAdvisor.isUsingAllocatedJudge()) {
            return true;
        } else {
            return isNotEmpty(judgeAndLegalAdvisor.getJudgeTitle());
        }
    }

    private String buildNextStepsLabel(CaseData caseData) {
        List<String> requiredMissingInformation = new ArrayList<>();
        requiredMissingInformation.add("## Next steps");
        requiredMissingInformation.add("Your order will be saved as a draft in 'Draft orders'.");
        requiredMissingInformation.add("You cannot seal and send the order until adding:");

        if (caseData.getFirstHearing().isEmpty()) {
            requiredMissingInformation.add("* the first hearing details");
        }

        if (isEmpty(caseData.getAllocatedJudge())) {
            requiredMissingInformation.add("* the allocated judge");
            if (isEmpty(caseData.getAllocationDecision())) {
                requiredMissingInformation.add("* the allocation decision");
            }
        }

        if (isEmpty(caseData.getGatekeepingOrderIssuingJudge().getJudgeTitle())) {
            requiredMissingInformation.add("* the judge issuing the order");
        }

        return String.join("\n\n", requiredMissingInformation);
    }
}
