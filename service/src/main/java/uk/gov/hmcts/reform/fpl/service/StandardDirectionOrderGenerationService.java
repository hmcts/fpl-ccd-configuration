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
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisDirection;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearingBooking;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRespondent;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisStandardDirectionOrder;

import java.io.IOException;
import java.util.List;

import static java.lang.String.format;
import static java.time.format.FormatStyle.LONG;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.lowerCase;
import static org.apache.commons.lang3.StringUtils.trim;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.model.configuration.Display.Due.BY;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getLegalAdvisorName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StandardDirectionOrderGenerationService extends
    DocmosisTemplateDataGeneration<DocmosisStandardDirectionOrder> {

    private final HearingBookingService hearingBookingService;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final OrdersLookupService ordersLookupService;
    private final HearingVenueLookUpService hearingVenueLookUpService;
    private final CommonCaseDataExtractionService dataExtractionService;

    public static final String DEFAULT = "";
    private static final int SDO_DIRECTION_INDEX_START = 2;

    public DocmosisStandardDirectionOrder getTemplateData(CaseData caseData) throws IOException {
        DocmosisStandardDirectionOrder.DocmosisStandardDirectionOrderBuilder orderBuilder =
            DocmosisStandardDirectionOrder.builder();

        Order standardDirectionOrder = caseData.getStandardDirectionOrder();

        orderBuilder
            .judgeAndLegalAdvisor(getJudgeAndLegalAdvisor(standardDirectionOrder))
            .courtName(hmctsCourtLookupConfiguration.getCourt(caseData.getCaseLocalAuthority()).getName())
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .dateOfIssue(standardDirectionOrder.getDateOfIssue())
            .complianceDeadline(formatLocalDateToString(caseData.getDateSubmitted().plusWeeks(26), LONG))
            .children(getChildrenDetails(caseData.getAllChildren()))
            .respondents(getRespondentsNameAndRelationship(caseData.getAllRespondents()))
            .respondentsProvided(isNotEmpty(caseData.getAllRespondents()))
            .applicantName(getApplicantName(caseData.findApplicant(0).orElse(Applicant.builder().build())))
            .directions(getGroupedDirections(standardDirectionOrder))
            .hearingBooking(getHearingBookingData(caseData.getHearingDetails()))
            .crest(getCrestData());

        if (SEALED == standardDirectionOrder.getOrderStatus()) {
            orderBuilder.courtseal(getCourtSealData());
        } else {
            orderBuilder.draftbackground(getDraftWaterMarkData());
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

    List<DocmosisChild> getChildrenDetails(List<Element<Child>> children) {
        return children.stream()
            .map(element -> element.getValue().getParty())
            .map(this::buildChild)
            .collect(toList());
    }

    private DocmosisChild buildChild(ChildParty child) {
        return DocmosisChild.builder()
            .name(child.getFullName())
            .gender(defaultIfNull(child.getGender(), DEFAULT))
            .dateOfBirth(getDateOfBirth(child))
            .build();
    }

    private String getDateOfBirth(ChildParty child) {
        return ofNullable(child.getDateOfBirth())
            .map(dateOfBirth -> formatLocalDateToString(dateOfBirth, LONG))
            .orElse(DEFAULT);
    }

    List<DocmosisRespondent> getRespondentsNameAndRelationship(List<Element<Respondent>> respondents) {
        return respondents.stream()
            .map(element -> element.getValue().getParty())
            .map(this::buildRespondent)
            .collect(toList());
    }

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
                            .body(trim(direction.getValue().getDirectionText()))
                            .build());
                    }
                }
                return formattedDirections.build();
            }
        ).orElse(ImmutableList.of());
    }

    String getApplicantName(Applicant applicant) {
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
        return format("%d. %s %s %s", index, direction.getDirectionType(), lowerCase(config.due.toString()),
            ofNullable(direction.getDateToBeCompletedBy())
                .map(date -> formatLocalDateTimeBaseUsingFormat(date, config.pattern))
                .orElse("unknown"));
    }

    private DocmosisHearingBooking getHearingBookingData(List<Element<HearingBooking>> hearingDetails) {
        return hearingBookingService.getFirstHearing(hearingDetails).map(hearing -> {
                HearingVenue hearingVenue = hearingVenueLookUpService.getHearingVenue(hearing);

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
