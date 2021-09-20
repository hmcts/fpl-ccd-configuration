package uk.gov.hmcts.reform.fpl.service.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.DirectionDueDateType;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CustomDirection;
import uk.gov.hmcts.reform.fpl.model.GatekeepingOrderSealDecision;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.StandardDirection;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.Display;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisDirection;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisStandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.event.GatekeepingOrderEventData;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.OrdersLookupService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.DirectionDueDateType.DAYS;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.formatCCDCaseNumber;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getSelectedJudge;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GatekeepingOrderGenerationService extends
    DocmosisTemplateDataGeneration<DocmosisStandardDirectionOrder> {
    private final CaseDataExtractionService dataService;
    private final OrdersLookupService ordersConfig;

    public DocmosisStandardDirectionOrder getTemplateData(CaseData caseData) {
        GatekeepingOrderEventData eventData = caseData.getGatekeepingOrderEventData();
        HearingBooking firstHearing = caseData.getFirstHearingOfType(HearingType.CASE_MANAGEMENT)
            .orElse(null);

        GatekeepingOrderSealDecision gatekeepingOrderSealDecision = eventData.getGatekeepingOrderSealDecision();

        JudgeAndLegalAdvisor judgeAndLegalAdvisor = getSelectedJudge(
            eventData.getGatekeepingOrderIssuingJudge(), caseData.getAllocatedJudge()
        );

        DocmosisStandardDirectionOrder.DocmosisStandardDirectionOrderBuilder<?, ?> orderBuilder =
            DocmosisStandardDirectionOrder.builder()
                .judgeAndLegalAdvisor(dataService.getJudgeAndLegalAdvisor((judgeAndLegalAdvisor)))
                .courtName(dataService.getCourtName(caseData))
                .familyManCaseNumber(caseData.getFamilyManCaseNumber())
                .ccdCaseNumber(formatCCDCaseNumber(caseData.getId()))
                .complianceDeadline(caseData.getComplianceDeadline())
                .children(dataService.getChildrenDetails(caseData.getAllChildren()))
                .respondents(dataService.getRespondentsNameAndRelationship(caseData.getAllRespondents()))
                .respondentsProvided(isNotEmpty(caseData.getAllRespondents()))
                .applicantName(dataService.getApplicantName(caseData))
                .directions(buildDirections(caseData))
                .hearingBooking(dataService.getHearingBookingData(firstHearing))
                .crest(getCrestData());

        if (SEALED.equals(gatekeepingOrderSealDecision.getOrderStatus())) {
            orderBuilder.courtseal(getCourtSealData());
            orderBuilder.dateOfIssue(formatLocalDateToString(gatekeepingOrderSealDecision.getDateOfIssue(), DATE));
        } else {
            orderBuilder.draftbackground(getDraftWaterMarkData());
            orderBuilder.dateOfIssue("<date will be added on issue>");
        }
        return orderBuilder.build();
    }

    private List<DocmosisDirection> buildDirections(CaseData caseData) {
        final List<Element<StandardDirection>> standardDirections = nullSafeList(caseData.getGatekeepingOrderEventData()
            .getStandardDirections());

        final List<Element<CustomDirection>> customDirections = nullSafeList(caseData
            .getGatekeepingOrderEventData().getCustomDirections());

        final AtomicInteger directionIndex = new AtomicInteger(1);

        return Stream.of(standardDirections, customDirections)
            .flatMap(Collection::stream)
            .map(Element::getValue)
            .sorted(comparing(StandardDirection::getAssignee))
            .map(direction -> toDocmosisDirection(direction, directionIndex.getAndAdd(1)))
            .collect(toList());
    }

    private DocmosisDirection toDocmosisDirection(StandardDirection direction, int index) {
        return DocmosisDirection.builder()
            .assignee(direction.getAssignee())
            .title(formatTitle(direction, index))
            .body(direction.getDescription())
            .build();
    }

    private String formatTitle(StandardDirection direction, int index) {
        final DirectionConfiguration directionConf = ordersConfig.getDirectionConfiguration(direction.getType());
        final DirectionDueDateType dueDateType = direction.getDueDateType();
        final Display display = directionConf.getDisplay();

        if (DAYS == dueDateType) {

            if (direction.getDaysBeforeHearing() == 0) {
                return format("%d. %s by the day of the hearing", index, direction.getTitle());
            }

            if (direction.getDaysBeforeHearing() == 1) {
                return format("%d. %s 1 working day before the hearing", index, direction.getTitle());
            }

            return format("%d. %s %d working days before the hearing", index, direction.getTitle(),
                direction.getDaysBeforeHearing());

        } else {
            LocalDateTime dueDate = direction.getDateToBeCompletedBy();

            String formattedDate = formatLocalDateTimeBaseUsingFormat(dueDate, display.getTemplateDateFormat());

            return format("%d. %s %s %s", index, direction.getTitle(), display.getDue().toString().toLowerCase(),
                formattedDate);
        }
    }
}
