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
import java.util.function.Function;

import static java.lang.String.format;
import static java.time.format.FormatStyle.LONG;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.lowerCase;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.model.configuration.Display.Due.BY;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getLegalAdvisorName;

//TODO: ensure everything is still working as expected - I don't think BLANK appears everywhere it used to. FPLA-1477
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StandardDirectionOrderGenerationService extends
    DocmosisTemplateDataGeneration<DocmosisStandardDirectionOrder> {

    //TODO: when should this be used? see FPLA-1087
    public static final String DEFAULT = "BLANK - please complete";
    private static final int SDO_DIRECTION_INDEX_START = 2;

    private final HearingBookingService hearingBookingService;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final OrdersLookupService ordersLookupService;
    private final CommonCaseDataExtractionService dataExtractionService;

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
            .children(dataExtractionService.getChildrenDetails(caseData.getAllChildren()))
            .respondents(dataExtractionService.getRespondentsNameAndRelationship(caseData.getAllRespondents()))
            .respondentsProvided(isNotEmpty(caseData.getAllRespondents()))
            .applicantName(dataExtractionService.getApplicantName(caseData.getAllApplicants()))
            .directions(getGroupedDirections(standardDirectionOrder))
            .hearingBooking(getHearingBookingData(caseData.getHearingDetails()));

        if (SEALED != standardDirectionOrder.getOrderStatus()) {
            orderBuilder.draftbackground(format(BASE_64, generateDraftWatermarkEncodedString()));
        }

        if (SEALED == standardDirectionOrder.getOrderStatus()) {
            orderBuilder.courtseal(format(BASE_64, generateCourtSealEncodedString()));
        }
        return orderBuilder.build();
    }

    private DocmosisJudgeAndLegalAdvisor getJudgeAndLegalAdvisor(Order standardDirectionOrder) {
        return dataExtractionService.getJudgeAndLegalAdvisor(standardDirectionOrder.getJudgeAndLegalAdvisor());
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
        HearingBooking hearingBooking = hearingBookingService.getFirstHearing(hearingDetails).orElse(null);

        return dataExtractionService.getHearingBookingData(hearingBooking, null);
    }
}
