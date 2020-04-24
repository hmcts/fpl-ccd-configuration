package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisDirection;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisStandardDirectionOrder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StandardDirectionOrderGenerationService extends
    DocmosisTemplateDataGeneration<DocmosisStandardDirectionOrder> {
    private final HearingBookingService hearingBookingService;
    private final OrdersLookupService ordersLookupService;
    private final CommonCaseDataExtractionService dataService;

    public static final String DEFAULT = "BLANK - please complete";
    private static final int SDO_DIRECTION_INDEX_START = 2;

    public DocmosisStandardDirectionOrder getTemplateData(CaseData caseData) throws IOException {
        Order standardDirectionOrder = caseData.getStandardDirectionOrder();

        HearingBooking firstHearing = hearingBookingService.getFirstHearing(caseData.getHearingDetails())
            .orElse(null);

        DocmosisStandardDirectionOrder.DocmosisStandardDirectionOrderBuilder orderBuilder =
            DocmosisStandardDirectionOrder.builder()
                .judgeAndLegalAdvisor(getJudgeAndLegalAdvisor(standardDirectionOrder.getJudgeAndLegalAdvisor()))
                .courtName(dataService.getCourtName(caseData.getCaseLocalAuthority()))
                .familyManCaseNumber(caseData.getFamilyManCaseNumber())
                .dateOfIssue(standardDirectionOrder.getDateOfIssue())
                .complianceDeadline(caseData.getComplianceDeadline())
                .children(dataService.getChildrenDetails(caseData.getAllChildren()))
                .respondents(dataService.getRespondentsNameAndRelationship(caseData.getAllRespondents()))
                .respondentsProvided(isNotEmpty(caseData.getAllRespondents()))
                .applicantName(dataService.getApplicantName(caseData.getAllApplicants()))
                .directions(buildDirections(standardDirectionOrder.getDirections()))
                .hearingBooking(dataService.getHearingBookingData(firstHearing, null));

        if (standardDirectionOrder.isDraft()) {
            orderBuilder.draftbackground(format(BASE_64, generateDraftWatermarkEncodedString()));
        }

        if (standardDirectionOrder.isSealed()) {
            orderBuilder.courtseal(format(BASE_64, generateCourtSealEncodedString()));
        }
        return orderBuilder.build();
    }

    private DocmosisJudgeAndLegalAdvisor getJudgeAndLegalAdvisor(JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        return dataService.getJudgeAndLegalAdvisor(judgeAndLegalAdvisor);
    }

    private List<DocmosisDirection> buildDirections(List<Element<Direction>> elements) throws IOException {
        List<Direction> directions = unwrapElements(elements);
        List<DirectionConfiguration> config = ordersLookupService.getStandardDirectionOrder().getDirections();
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
