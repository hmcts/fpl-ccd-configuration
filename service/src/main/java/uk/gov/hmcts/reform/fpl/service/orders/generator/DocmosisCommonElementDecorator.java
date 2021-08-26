package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.DocmosisImages;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.formatCCDCaseNumber;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getSelectedJudge;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DocmosisCommonElementDecorator {

    private final ChildrenSmartSelector childrenSmartSelector;
    private final CaseDataExtractionService extractionService;

    public DocmosisParameters decorate(DocmosisParameters currentParameters, CaseData caseData,
                                       OrderStatus status, Order orderType) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        List<Element<Child>> selectedChildren = childrenSmartSelector.getSelectedChildren(caseData);
        List<DocmosisChild> children = extractionService.getChildrenDetails(selectedChildren);

        JudgeAndLegalAdvisor judgeAndLegalAdvisor = getSelectedJudge(
            caseData.getJudgeAndLegalAdvisor(), caseData.getAllocatedJudge()
        );
        DocmosisJudgeAndLegalAdvisor docmosisJudgeAndLegalAdvisor =
            extractionService.getJudgeAndLegalAdvisor(judgeAndLegalAdvisor);

        String dateOfIssue = currentParameters.getDateOfIssue();

        DocmosisParameters.DocmosisParametersBuilder<?, ?> docmosisParametersBuilder = currentParameters.toBuilder();

        if (Objects.isNull(currentParameters.getChildrenAct())) {
            docmosisParametersBuilder.childrenAct(orderType.getChildrenAct());
        }

        return docmosisParametersBuilder
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .ccdCaseNumber(formatCCDCaseNumber(caseData.getId()))
            .judgeAndLegalAdvisor(docmosisJudgeAndLegalAdvisor)
            .courtName(extractionService.getCourtName(caseData))
            .dateOfIssue(isBlank(dateOfIssue)
                ? formatLocalDateToString(eventData.getManageOrdersApprovalDate(), DATE) : dateOfIssue)
            .children(children)
            .crest(DocmosisImages.CREST.getValue())
            .draftbackground(DRAFT == status ? DocmosisImages.DRAFT_WATERMARK.getValue() : null)
            .courtseal(SEALED == status ? DocmosisImages.COURT_SEAL.getValue(caseData.getImageLanguage()) : null)
            .build();
    }
}
