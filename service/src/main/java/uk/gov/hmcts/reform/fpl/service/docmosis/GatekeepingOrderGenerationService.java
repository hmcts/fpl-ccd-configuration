package uk.gov.hmcts.reform.fpl.service.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CustomDirection;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.SaveOrSendGatekeepingOrder;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisDirection;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisStandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.formatCCDCaseNumber;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GatekeepingOrderGenerationService extends
    DocmosisTemplateDataGeneration<DocmosisStandardDirectionOrder> {
    private final CaseDataExtractionService dataService;

    public DocmosisStandardDirectionOrder getTemplateData(CaseData caseData) {
        HearingBooking firstHearing = caseData.getFirstHearingOfType(HearingType.CASE_MANAGEMENT)
            .orElse(null);

        SaveOrSendGatekeepingOrder saveOrSendGatekeepingOrder = caseData.getSaveOrSendGatekeepingOrder();

        DocmosisStandardDirectionOrder.DocmosisStandardDirectionOrderBuilder<?, ?> orderBuilder =
            DocmosisStandardDirectionOrder.builder()
                // is this the right judge?
                .judgeAndLegalAdvisor(dataService.getJudgeAndLegalAdvisor((caseData.getJudgeAndLegalAdvisor())))
                .courtName(dataService.getCourtName(caseData.getCaseLocalAuthority()))
                .familyManCaseNumber(caseData.getFamilyManCaseNumber())
                .ccdCaseNumber(formatCCDCaseNumber(caseData.getId()))
                .complianceDeadline(caseData.getComplianceDeadline())
                .children(dataService.getChildrenDetails(caseData.getAllChildren()))
                .respondents(dataService.getRespondentsNameAndRelationship(caseData.getAllRespondents()))
                .respondentsProvided(isNotEmpty(caseData.getAllRespondents()))
                .applicantName(dataService.getApplicantName(caseData.getAllApplicants()))
                .directions(buildCustomDirections(caseData.getSdoDirectionCustom()))
                .hearingBooking(dataService.getHearingBookingData(firstHearing))
                .crest(getCrestData());

        if (SEALED.equals(saveOrSendGatekeepingOrder.getOrderStatus())) {
            orderBuilder.courtseal(getCourtSealData());
            orderBuilder.dateOfIssue(formatLocalDateToString(saveOrSendGatekeepingOrder.getDateOfIssue(), DATE));
        } else {
            orderBuilder.draftbackground(getDraftWaterMarkData());
        }
        return orderBuilder.build();
    }

    private List<DocmosisDirection> buildCustomDirections(List<Element<CustomDirection>> elements) {
        List<CustomDirection> customDirections = unwrapElements(elements);
        List<DocmosisDirection> formattedDirections = new ArrayList<>();
        for (CustomDirection direction : customDirections) {
            formattedDirections.add(DocmosisDirection.builder()
                .assignee(direction.getAssignee())
                .title(direction.getTitle())
                .body(direction.getDescription())
                .build());
        }
        return formattedDirections;
    }
}
