package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
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

import static java.lang.String.format;
import static java.time.format.FormatStyle.LONG;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.lowerCase;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.model.configuration.Display.Due.BY;
import static uk.gov.hmcts.reform.fpl.service.DateFormatterService.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.service.DateFormatterService.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.service.DateFormatterService.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.service.DocmosisTemplateDataGeneration.BASE_64;
import static uk.gov.hmcts.reform.fpl.service.DocmosisTemplateDataGeneration.generateDraftWatermarkEncodedString;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getLegalAdvisorName;

//TODO: ensure everything is still working as expected - I don't think BLANK appears everywhere it used to. FPLA-1477
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseDataExtractionService {
    private final HearingBookingService hearingBookingService;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final OrdersLookupService ordersLookupService;
    private final HearingVenueLookUpService hearingVenueLookUpService;
    private final CommonCaseDataExtractionService dataExtractionService;

    //TODO: when should this be used? see FPLA-1087
    public static final String DEFAULT = "BLANK - please complete";
    private static final int SDO_DIRECTION_INDEX_START = 2;

    public DocmosisStandardDirectionOrder getStandardOrderDirectionData(CaseData caseData) throws IOException {
        DocmosisStandardDirectionOrder.Builder orderBuilder = DocmosisStandardDirectionOrder.builder();
        Order standardDirectionOrder = caseData.getStandardDirectionOrder();

        orderBuilder
            .judgeAndLegalAdvisor(getJudgeAndLegalAdvisor(standardDirectionOrder))
            .courtName(hmctsCourtLookupConfiguration.getCourt(caseData.getCaseLocalAuthority()).getName())
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .generationDate(formatLocalDateToString(LocalDate.now(), LONG))
            .complianceDeadline(formatLocalDateToString(caseData.getDateSubmitted().plusWeeks(26), LONG))
            .children(getChildrenDetails(caseData.getAllChildren()))
            .respondents(getRespondentsNameAndRelationship(caseData.getAllRespondents()))
            .respondentsProvided(isNotEmpty(caseData.getAllRespondents()))
            .applicantName(getApplicantName(caseData.findApplicant(0).orElse(Applicant.builder().build())))
            .directions(getGroupedDirections(standardDirectionOrder))
            .hearingBooking(getHearingBookingData(caseData.getHearingDetails()));

        if (SEALED != standardDirectionOrder.getOrderStatus()) {
            orderBuilder.draftbackground(format(BASE_64, generateDraftWatermarkEncodedString()));
        }
        return orderBuilder.build();
    }

    private DocmosisJudgeAndLegalAdvisor getJudgeAndLegalAdvisor(Order standardDirectionOrder) {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = standardDirectionOrder.getJudgeAndLegalAdvisor();

        return DocmosisJudgeAndLegalAdvisor.builder()
            .judgeTitleAndName(formatJudgeTitleAndName(judgeAndLegalAdvisor))
            .legalAdvisorName(getLegalAdvisorName(judgeAndLegalAdvisor))
            .build();
    }

    private List<DocmosisChildren> getChildrenDetails(List<Element<Child>> children) {
        return children.stream()
            .map(element -> element.getValue().getParty())
            .map(this::buildChild)
            .collect(toList());
    }

    // TODO: see FPLA-1087
    private DocmosisChildren buildChild(ChildParty child) {
        return DocmosisChildren.builder()
            .name(child.getFullName())
            .gender(defaultIfNull(child.getGender(), DEFAULT))
            .dateOfBirth(getDateOfBirth(child))
            .build();
    }

    // TODO: see FPLA-1087
    private String getDateOfBirth(ChildParty child) {
        return ofNullable(child.getDateOfBirth())
            .map(dateOfBirth -> formatLocalDateToString(dateOfBirth, LONG))
            .orElse(DEFAULT);
    }

    private List<DocmosisRespondent> getRespondentsNameAndRelationship(List<Element<Respondent>> respondents) {
        return respondents.stream()
            .map(element -> element.getValue().getParty())
            .map(this::buildRespondent)
            .collect(toList());
    }

    // TODO: see FPLA-1087
    private DocmosisRespondent buildRespondent(RespondentParty respondent) {
        return DocmosisRespondent.builder()
            .name(respondent.getFullName())
            .relationshipToChild(defaultIfNull(respondent.getRelationshipToChild(), DEFAULT))
            .build();
    }

    private List<DocmosisDirection> getGroupedDirections(Order order) throws IOException {
        OrderDefinition configOrder = ordersLookupService.getStandardDirectionOrder();

        return ofNullable(order.getDirections()).map(directions -> {
                ImmutableList.Builder<DocmosisDirection> formattedDirections = ImmutableList.builder();

                int directionNumber = SDO_DIRECTION_INDEX_START;
                for (Element<Direction> direction : directions) {
                    if (!"No".equals(direction.getValue().getDirectionNeeded())) {
                        formattedDirections.add(DocmosisDirection.builder()
                            .assignee(direction.getValue().getAssignee())
                            .title(formatTitle(directionNumber++, direction.getValue(), configOrder.getDirections()))
                            .body(direction.getValue().getDirectionText())
                            .build());
                    }
                }
                return formattedDirections.build();
            }
        ).orElse(ImmutableList.of());
    }

    private String getApplicantName(Applicant applicant) {
        return ofNullable(applicant.getParty())
            .map(ApplicantParty::getOrganisationName)
            .orElse("");
    }

    private String formatTitle(int index, Direction direction, List<DirectionConfiguration> directionConfigurations) {

        // default values here cover edge case where direction title is not found in configuration. Reusable for CMO?
        @NoArgsConstructor
        class DateFormattingConfig {
            private String pattern = TIME_DATE;
            private Display.Due due = BY;
        }

        final DateFormattingConfig config = new DateFormattingConfig();

        // find the date configuration values for the given direction
        for (DirectionConfiguration directionConfiguration : directionConfigurations) {
            if (directionConfiguration.getTitle().equals(direction.getDirectionType())) {
                Display display = directionConfiguration.getDisplay();
                config.pattern = display.getTemplateDateFormat();
                config.due = display.getDue();
                break;
            }
        }

        // create direction display title for docmosis in format "index. directionTitle (by / on) date"
        // TODO: see FPLA-1087
        return format("%d. %s %s %s", index, direction.getDirectionType(), lowerCase(config.due.toString()),
            ofNullable(direction.getDateToBeCompletedBy())
                .map(date -> formatLocalDateTimeBaseUsingFormat(date, config.pattern))
                .orElse("unknown"));
    }

    private DocmosisHearingBooking getHearingBookingData(List<Element<HearingBooking>> hearingDetails) {
        return ofNullable(hearingDetails).map(hearingBookings -> {
                HearingBooking hearing = hearingBookingService.getMostUrgentHearingBooking(hearingBookings);
                HearingVenue hearingVenue = hearingVenueLookUpService.getHearingVenue(hearing.getVenue());

                return DocmosisHearingBooking.builder()
                    .hearingDate(dataExtractionService.getHearingDateIfHearingsOnSameDay(hearing).orElse(""))
                    .hearingVenue(hearingVenueLookUpService.buildHearingVenue(hearingVenue))
                    .preHearingAttendance(dataExtractionService.extractPrehearingAttendance(hearing))
                    .hearingTime(dataExtractionService.getHearingTime(hearing))
                    .hearingJudgeTitleAndName(formatJudgeTitleAndName(hearing.getJudgeAndLegalAdvisor()))
                    .hearingLegalAdvisorName(getLegalAdvisorName(hearing.getJudgeAndLegalAdvisor()))
                    .build();
            }
        ).orElse(DocmosisHearingBooking.builder().build());
    }
}
