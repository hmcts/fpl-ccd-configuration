package uk.gov.hmcts.reform.fpl.service.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisDirection;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisStandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.OrdersLookupService;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.formatCCDCaseNumber;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StandardDirectionOrderGenerationService extends
    DocmosisTemplateDataGeneration<DocmosisStandardDirectionOrder> {
    private final OrdersLookupService ordersLookupService;
    private final CaseDataExtractionService dataService;

    private static final int SDO_DIRECTION_INDEX_START = 2;

    public DocmosisStandardDirectionOrder getTemplateData(CaseData caseData) {
        StandardDirectionOrder standardDirectionOrder = caseData.getStandardDirectionOrder();

        HearingBooking firstHearing = caseData.getFirstHearingOfType(HearingType.CASE_MANAGEMENT)
            .orElse(null);

        DocmosisStandardDirectionOrder.DocmosisStandardDirectionOrderBuilder<?, ?> orderBuilder =
            DocmosisStandardDirectionOrder.builder()
                .judgeAndLegalAdvisor(getJudgeAndLegalAdvisor(standardDirectionOrder.getJudgeAndLegalAdvisor()))
                .courtName(dataService.getCourtName(caseData.getCaseLocalAuthority()))
                .familyManCaseNumber(caseData.getFamilyManCaseNumber())
                .ccdCaseNumber(formatCCDCaseNumber(caseData.getId()))
                .dateOfIssue(standardDirectionOrder.getDateOfIssue())
                .complianceDeadline(caseData.getComplianceDeadline())
                .children(dataService.getChildrenDetails(caseData.getAllChildren()))
                .respondents(dataService.getRespondentsNameAndRelationship(caseData.getAllRespondents()))
                .respondentsProvided(isNotEmpty(caseData.getAllRespondents()))
                .applicantName(dataService.getApplicantName(caseData.getAllApplicants()))
                .directions(buildDirections(standardDirectionOrder.getDirections()))
                .hearingBooking(dataService.getHearingBookingData(firstHearing))
                .crest(getCrestData());

        if (standardDirectionOrder.isSealed()) {
            orderBuilder.courtseal(getCourtSealData());
        } else {
            orderBuilder.draftbackground(getDraftWaterMarkData());
        }
        return orderBuilder.build();
    }

    private DocmosisJudgeAndLegalAdvisor getJudgeAndLegalAdvisor(JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        return dataService.getJudgeAndLegalAdvisor(judgeAndLegalAdvisor);
    }

    private List<DocmosisDirection> buildDirections(List<Element<Direction>> elements) {
        List<Direction> directions = unwrapElements(elements);
        List<DirectionConfiguration> config = ordersLookupService.getStandardDirectionOrder().getStandardDirections();
        List<DocmosisDirection> formattedDirections = new ArrayList<>();
        int index = SDO_DIRECTION_INDEX_START;

        for (Direction direction : directions) {
            if (direction.isNeeded()) {
                DocmosisDirection.Builder builder = dataService.baseDirection(direction, index++, config);
                formattedDirections.add(builder.build());
            }
        }
        return formattedDirections;
    }
}
