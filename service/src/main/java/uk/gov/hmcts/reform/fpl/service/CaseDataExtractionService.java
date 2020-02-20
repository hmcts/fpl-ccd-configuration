package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.Display;
import uk.gov.hmcts.reform.fpl.model.configuration.OrderDefinition;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChildren;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisDirection;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearingBooking;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRespondent;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisStandardDirectionOrder;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.time.format.FormatStyle.LONG;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.service.DateFormatterService.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.service.DateFormatterService.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.service.DocmosisTemplateDataGeneration.generateDraftWatermarkEncodedString;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getLegalAdvisorName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseDataExtractionService {

    //TODO: when should this be used?
    //TODO: ensure everything is still working as expected - I don't think BLANK appears everywhere it used to.
    public static final String DEFAULT = "BLANK - please complete";
    private final HearingBookingService hearingBookingService;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final OrdersLookupService ordersLookupService;
    private final CommonDirectionService directionService;
    private final HearingVenueLookUpService hearingVenueLookUpService;
    private final CommonCaseDataExtractionService dataExtractionService;

    public DocmosisStandardDirectionOrder getStandardOrderDirectionData(CaseData caseData) throws IOException {
        DocmosisStandardDirectionOrder.Builder builder = DocmosisStandardDirectionOrder.builder();

        builder.judgeAndLegalAdvisor(getJudgeAndLegalAdvisor(caseData.getStandardDirectionOrder()));
        builder.courtName(hmctsCourtLookupConfiguration.getCourt(caseData.getCaseLocalAuthority()).getName());
        builder.familyManCaseNumber(caseData.getFamilyManCaseNumber());
        builder.generationDate(formatLocalDateToString(LocalDate.now(), LONG));
        builder.complianceDeadline(formatLocalDateToString(caseData.getDateSubmitted().plusWeeks(26), LONG));
        builder.children(getChildrenDetails(caseData.getAllChildren()));
        builder.respondents(getRespondentsNameAndRelationship(caseData.getAllRespondents()));
        builder.respondentsProvided(isNotEmpty(caseData.getAllRespondents()));
        builder.applicantName(caseData.findApplicant(0).map(x -> x.getParty().getOrganisationName()).orElse(""));
        builder.directions(getGroupedDirections(caseData));
        builder.hearingBooking(getHearingBookingData(caseData));

        if (caseData.getStandardDirectionOrder().getOrderStatus() != SEALED) {
            builder.draftbackground(String.format("image:base64:%1$s", generateDraftWatermarkEncodedString()));
        }

        return builder.build();
    }

    private DocmosisJudgeAndLegalAdvisor getJudgeAndLegalAdvisor(Order standardDirectionOrder) {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = standardDirectionOrder.getJudgeAndLegalAdvisor();

        return DocmosisJudgeAndLegalAdvisor.builder()
            .judgeTitleAndName(formatJudgeTitleAndName(judgeAndLegalAdvisor))
            .legalAdvisorName(getLegalAdvisorName(judgeAndLegalAdvisor))
            .build();
    }

    //TODO: look into and potentially refactor -> handled in hearingService?
    private DocmosisHearingBooking getHearingBookingData(CaseData caseData) {
        if (caseData.getHearingDetails() == null || caseData.getHearingDetails().isEmpty()) {
            return DocmosisHearingBooking.builder().build();
        }

        HearingBooking prioritisedHearingBooking = hearingBookingService.getMostUrgentHearingBooking(caseData
            .getHearingDetails());

        HearingVenue hearingVenue = hearingVenueLookUpService.getHearingVenue(prioritisedHearingBooking.getVenue());

        return DocmosisHearingBooking.builder()
            .hearingDate(dataExtractionService.getHearingDateIfHearingsOnSameDay(prioritisedHearingBooking).orElse(""))
            .hearingVenue(hearingVenueLookUpService.buildHearingVenue(hearingVenue))
            .preHearingAttendance(dataExtractionService.extractPrehearingAttendance(prioritisedHearingBooking))
            .hearingTime(dataExtractionService.getHearingTime(prioritisedHearingBooking))
            .hearingJudgeTitleAndName(formatJudgeTitleAndName(prioritisedHearingBooking.getJudgeAndLegalAdvisor()))
            .hearingLegalAdvisorName(getLegalAdvisorName(prioritisedHearingBooking.getJudgeAndLegalAdvisor()))
            .build();
    }

    //TODO: look into and potentially refactor -> something to exist in directions service?
    private List<DocmosisDirection> getGroupedDirections(CaseData caseData) throws IOException {
        OrderDefinition standardDirectionOrder = ordersLookupService.getStandardDirectionOrder();

        if (caseData.getStandardDirectionOrder().getDirections() == null) {
            return emptyList();
        }

        List<Element<Direction>> numberedDirections =
            directionService.numberDirections(caseData.getStandardDirectionOrder().getDirections());

        Map<DirectionAssignee, List<Element<Direction>>> groupedDirections =
            directionService.sortDirectionsByAssignee(numberedDirections);

        ImmutableList.Builder<DocmosisDirection> formattedDirections = ImmutableList.builder();

        groupedDirections.forEach((assignee, directions) ->
            formattedDirections.addAll(directions.stream()
                .map(Element::getValue)
                .filter(direction -> !"No".equals(direction.getDirectionNeeded()))
                .map(direction -> DocmosisDirection.builder()
                    .assignee(assignee)
                    .title(formatTitle(direction, standardDirectionOrder.getDirections()))
                    .body(direction.getDirectionText())
                    .build())
                .collect(toList())));

        return formattedDirections.build();
    }

    private List<DocmosisRespondent> getRespondentsNameAndRelationship(List<Element<Respondent>> respondents) {
        return respondents.stream()
            .map(element -> element.getValue().getParty())
            .map(respondent -> DocmosisRespondent.builder()
                .name(respondent.getFullName())
                .relationshipToChild(defaultIfNull(respondent.getRelationshipToChild(), DEFAULT))
                .build())
            .collect(toList());
    }

    private List<DocmosisChildren> getChildrenDetails(List<Element<Child>> children) {
        return children.stream()
            .map(element -> element.getValue().getParty())
            .map(child -> DocmosisChildren.builder()
                .name(child.getFullName())
                .gender(defaultIfNull(child.getGender(), DEFAULT))
                .dateOfBirth(getDateOfBirth(child))
                .build())
            .collect(toList());
    }

    private String getDateOfBirth(ChildParty child) {
        return child.getDateOfBirth() == null ? DEFAULT : formatLocalDateToString(child.getDateOfBirth(), LONG);
    }

    //TODO: look into and potentially refactor -> this seems totally separate to data extraction
    private String formatTitle(Direction direction, List<DirectionConfiguration> directions) {
        @AllArgsConstructor
        @NoArgsConstructor
        @Data
        class DateFormattingConfig {
            private String pattern = "h:mma, d MMMM yyyy";
            private Display.Due due = Display.Due.BY;
        }

        DateFormattingConfig dateFormattingConfig = directions.stream()
            .filter(directionConfiguration ->
                directionConfiguration.getTitle().equals(direction.getDirectionType().substring(3)))
            .map(DirectionConfiguration::getDisplay)
            .map(display -> new DateFormattingConfig(display.getTemplateDateFormat(), display.getDue()))
            .findAny()
            .orElseGet(DateFormattingConfig::new);

        return String.format(
            "%s %s %s", direction.getDirectionType(), dateFormattingConfig.due.toString().toLowerCase(),
            (direction.getDateToBeCompletedBy() != null
                ? formatLocalDateTimeBaseUsingFormat(direction.getDateToBeCompletedBy(),
                dateFormattingConfig.getPattern()) : "unknown"));
    }
}
