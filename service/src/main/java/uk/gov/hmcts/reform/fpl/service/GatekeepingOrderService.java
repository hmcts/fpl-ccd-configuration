package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.SaveOrSendGatekeepingOrder;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisStandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.event.GatekeepingOrderEventData;
import uk.gov.hmcts.reform.fpl.service.docmosis.GatekeepingOrderGenerationService;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.SDO;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getJudgeForTabView;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GatekeepingOrderService {
    private final DocumentService documentService;
    private final GatekeepingOrderGenerationService gatekeepingOrderGenerationService;

    public SaveOrSendGatekeepingOrder buildSaveOrSendPage(CaseData caseData) {
        //add draft document
        Document document = buildDocument(caseData);

        return SaveOrSendGatekeepingOrder.builder()
            .draftDocument(buildFromDocument(document))
            .nextSteps(buildNextStepsLabel(caseData))
            .orderStatus(null)
            .build();
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
            //after judge title selected the other fields are mandatory, so checking title verifies judge entry (?)
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

        if (!hasEnteredIssuingJudge(caseData.getGatekeepingOrderEventData().getGatekeepingOrderIssuingJudge())) {
            requiredMissingInformation.add("* the judge issuing the order");
        }

        if (requiredMissingInformation.isEmpty()) {
            return null;
        } else {
            String nextStepsLabel = "## Next steps\n\n"
                + "Your order will be saved as a draft in 'Draft orders'.\n\n"
                + "You cannot seal and send the order until adding:";
            requiredMissingInformation.add(0, nextStepsLabel);

            return String.join("\n\n", requiredMissingInformation);
        }
    }

    public StandardDirectionOrder buildBaseGatekeepingOrder(CaseData caseData) {
        GatekeepingOrderEventData eventData = caseData.getGatekeepingOrderEventData();

        JudgeAndLegalAdvisor judgeAndLegalAdvisor =
            getJudgeForTabView(eventData.getGatekeepingOrderIssuingJudge(), caseData.getAllocatedJudge());

        return StandardDirectionOrder.builder()
            .customDirections(eventData.getSdoDirectionCustom())
            .orderStatus(defaultIfNull(eventData.getSaveOrSendGatekeepingOrder().getOrderStatus(), DRAFT))
            .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
            .build();
    }

    public List<DocmosisTemplates> getNoticeOfProceedingsTemplates(CaseData caseData) {
        List<DocmosisTemplates> templates = new ArrayList<>();
        templates.add(DocmosisTemplates.C6);

        if (!caseData.getAllOthers().isEmpty()) {
            templates.add(DocmosisTemplates.C6A);
        }

        return templates;
    }

    public Document buildDocument(CaseData caseData) {
        DocmosisStandardDirectionOrder templateData = gatekeepingOrderGenerationService.getTemplateData(caseData);
        return documentService.getDocumentFromDocmosisOrderTemplate(templateData, SDO);
    }
}
