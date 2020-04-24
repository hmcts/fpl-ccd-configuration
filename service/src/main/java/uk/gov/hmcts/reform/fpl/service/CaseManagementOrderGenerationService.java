package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.OtherPartiesDirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.ParentsAndRespondentsDirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.Recital;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisDirection;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearingBooking;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRecital;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRepresentative;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRepresentedBy;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRespondent;
import uk.gov.hmcts.reform.fpl.model.interfaces.Representable;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.io.IOException;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static uk.gov.hmcts.reform.fpl.service.StandardDirectionOrderGenerationService.DEFAULT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getLegalAdvisorName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseManagementOrderGenerationService extends DocmosisTemplateDataGeneration<DocmosisCaseManagementOrder> {
    private static final String HEARING_EMPTY_PLACEHOLDER = "This will appear on the issued CMO";

    private final CommonCaseDataExtractionService dataExtractionService;
    private final DraftCMOService cmoService;
    private final HearingBookingService hearingBookingService;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final StandardDirectionOrderGenerationService standardDirectionOrderGenerationService;
    private final HearingVenueLookUpService hearingVenueLookUpService;
    private final Time time;

    public DocmosisCaseManagementOrder getTemplateData(CaseData caseData) throws IOException {
        List<Element<HearingBooking>> hearingDetails = caseData.getHearingDetails();
        CaseManagementOrder caseManagementOrder = cmoService.prepareCMO(caseData, caseData.getCaseManagementOrder());

        HearingBooking nextHearing = null;

        if (needsNextHearingDate(caseManagementOrder)) {
            UUID nextHearingId = caseManagementOrder.getNextHearing().getId();
            nextHearing = hearingBookingService.getHearingBookingByUUID(hearingDetails, nextHearingId);
        }

        HearingBooking hearingBooking = hearingBookingService.getHearingBooking(hearingDetails,
            caseData.getCmoHearingDateList());

        DocmosisCaseManagementOrder.DocmosisCaseManagementOrderBuilder order = DocmosisCaseManagementOrder.builder()
            .judgeAndLegalAdvisor(getJudgeAndLegalAdvisorData(hearingBooking.getJudgeAndLegalAdvisor()))
            .courtName(hmctsCourtLookupConfiguration.getCourt(caseData.getCaseLocalAuthority()).getName())
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .dateOfIssue(formatLocalDateToString(time.now().toLocalDate(), FormatStyle.LONG))
            .complianceDeadline(formatLocalDateToString(caseData.getDateSubmitted().plusWeeks(26), FormatStyle.LONG))
            .children(standardDirectionOrderGenerationService.getChildrenDetails(caseData.getAllChildren()))
            .respondents(getRespondentsNameAndRelationship(caseData))
            .respondentsProvided(isNotEmpty(caseData.getAllRespondents()))
            .applicantName(getApplicantName(caseData))
            .directions(getGroupedCMODirections(caseManagementOrder.getDirections()))
            .hearingBooking(getHearingBookingData(nextHearing))
            .representatives(getRepresentatives(caseData))
            .recitals(buildRecitals(caseManagementOrder.getRecitals()))
            .recitalsProvided(isNotEmpty(buildRecitals(caseManagementOrder.getRecitals())))
            .schedule(caseManagementOrder.getSchedule())
            .scheduleProvided("Yes".equals(getScheduleProvided(caseManagementOrder)));

        if (caseManagementOrder.isDraft()) {
            order.draftbackground(format(BASE_64, generateDraftWatermarkEncodedString()));
        }

        if (!caseManagementOrder.isDraft()) {
            order.courtseal(format(BASE_64, generateCourtSealEncodedString()));
        }

        return order.build();
    }

    private String getApplicantName(CaseData caseData) {
        return standardDirectionOrderGenerationService.getApplicantName(caseData.findApplicant(0)
            .orElse(Applicant.builder().build()));
    }

    private List<DocmosisRespondent> getRespondentsNameAndRelationship(CaseData caseData) {
        return standardDirectionOrderGenerationService.getRespondentsNameAndRelationship(caseData.getAllRespondents());
    }

    private boolean needsNextHearingDate(CaseManagementOrder caseManagementOrder) {
        return caseManagementOrder.getNextHearing() != null && caseManagementOrder.getNextHearing().getId() != null
            && !caseManagementOrder.isDraft();
    }

    private String getScheduleProvided(CaseManagementOrder caseManagementOrder) {
        return ofNullable(caseManagementOrder.getSchedule())
            .map(Schedule::getIncludeSchedule)
            .orElse("No");
    }

    private DocmosisJudgeAndLegalAdvisor getJudgeAndLegalAdvisorData(JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        return DocmosisJudgeAndLegalAdvisor.builder()
            .judgeTitleAndName(defaultIfBlank(formatJudgeTitleAndName(judgeAndLegalAdvisor), DEFAULT))
            .legalAdvisorName(getLegalAdvisorName(judgeAndLegalAdvisor))
            .build();
    }

    private DocmosisHearingBooking getHearingBookingData(HearingBooking hearingBooking) {
        return ofNullable(hearingBooking).map(hearing -> {
                HearingVenue hearingVenue = hearingVenueLookUpService.getHearingVenue(hearing);

                return DocmosisHearingBooking.builder()
                    .hearingDate(dataExtractionService.getHearingDateIfHearingsOnSameDay(hearing).orElse(""))
                    .hearingVenue(hearingVenueLookUpService.buildHearingVenue(hearingVenue))
                    .preHearingAttendance(dataExtractionService.extractPrehearingAttendance(hearing))
                    .hearingTime(dataExtractionService.getHearingTime(hearing))
                    .build();
            }
        ).orElse(DocmosisHearingBooking.builder()
            .hearingDate(HEARING_EMPTY_PLACEHOLDER)
            .hearingVenue(HEARING_EMPTY_PLACEHOLDER)
            .preHearingAttendance(HEARING_EMPTY_PLACEHOLDER)
            .hearingTime(HEARING_EMPTY_PLACEHOLDER)
            .build());
    }

    private List<DocmosisRepresentative> getRepresentatives(CaseData caseData) {
        List<DocmosisRepresentative> representativesInfo = new ArrayList<>();
        List<Element<Representative>> representatives = caseData.getRepresentatives();

        String applicantName = caseData.findApplicant(0)
            .map(element -> element.getParty().getOrganisationName())
            .orElse("");

        Solicitor solicitor = caseData.getSolicitor();

        representativesInfo.add(getApplicantDetails(applicantName, solicitor));

        unwrapElements(caseData.getRespondents1()).stream()
            .filter(respondent -> isNotEmpty(respondent.getRepresentedBy()))
            .forEach(respondent -> representativesInfo.add(DocmosisRepresentative.builder()
                .name(defaultIfNull(respondent.getParty().getFullName(), EMPTY))
                .representedBy(getRepresentativesInfo(respondent, representatives))
                .build()));


        unwrapElements(caseData.getAllOthers()).stream()
            .filter(other -> isNotEmpty(other.getRepresentedBy()))
            .forEach(other -> representativesInfo.add(DocmosisRepresentative.builder()
                .name(defaultIfNull(other.getName(), EMPTY))
                .representedBy(getRepresentativesInfo(other, representatives))
                .build()));

        return representativesInfo;
    }

    private List<DocmosisRepresentedBy> getRepresentativesInfo(Representable representable,
                                                               List<Element<Representative>> representatives) {
        return representable.getRepresentedBy().stream()
            .map(representativeId -> findRepresentative(representatives, representativeId.getValue()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(this::buildRepresentativeInfo)
            .collect(Collectors.toList());
    }

    private Optional<Representative> findRepresentative(List<Element<Representative>> representatives, UUID id) {
        return representatives.stream()
            .filter(representative -> representative.getId().equals(id))
            .findFirst()
            .map(Element::getValue);
    }

    private DocmosisRepresentedBy buildRepresentativeInfo(Representative representative) {
        return DocmosisRepresentedBy.builder()
            .name(representative.getFullName())
            .email(defaultIfNull(representative.getEmail(), EMPTY))
            .phoneNumber(defaultIfNull(representative.getTelephoneNumber(), EMPTY))
            .build();
    }

    private DocmosisRepresentative getApplicantDetails(String applicantName, Solicitor solicitor) {
        DocmosisRepresentative.DocmosisRepresentativeBuilder applicantDetails = DocmosisRepresentative.builder();

        applicantDetails.name(defaultIfBlank(applicantName, DEFAULT));

        if (solicitor == null) {
            applicantDetails.representedBy(List.of(DocmosisRepresentedBy.builder()
                .name(DEFAULT)
                .email(DEFAULT)
                .phoneNumber(DEFAULT)
                .build()));
        } else {
            String phoneNumber = defaultIfBlank(solicitor.getTelephone(), solicitor.getMobile());
            applicantDetails.representedBy(List.of(
                DocmosisRepresentedBy.builder()
                    .name(defaultIfBlank(solicitor.getName(), DEFAULT))
                    .email(defaultIfBlank(solicitor.getEmail(), DEFAULT))
                    .phoneNumber(defaultIfBlank(phoneNumber, DEFAULT))
                    .build()));
        }

        return applicantDetails.build();
    }

    private List<DocmosisDirection> getGroupedCMODirections(List<Element<Direction>> directions) {
        return ofNullable(directions).map(this::buildDocmosisDirections).orElse(ImmutableList.of());
    }

    private ImmutableList<DocmosisDirection> buildDocmosisDirections(List<Element<Direction>> directions) {
        ImmutableList.Builder<DocmosisDirection> formattedDirections = ImmutableList.builder();

        int directionNumber = 2;
        ParentsAndRespondentsDirectionAssignee respondentHeader = null;
        OtherPartiesDirectionAssignee otherHeader = null;

        for (Element<Direction> element : directions) {
            Direction direction = element.getValue();
            DocmosisDirection.Builder builder = buildBaseDirection(direction, directionNumber++);

            if (hasNewRespondentAssignee(respondentHeader, direction)) {
                respondentHeader = direction.getParentsAndRespondentsAssignee();
                builder.header("For " + respondentHeader.getLabel());
            }

            if (hasNewOtherAssignee(otherHeader, direction)) {
                otherHeader = direction.getOtherPartiesAssignee();
                builder.header("For " + otherHeader.getLabel());
            }

            formattedDirections.add(builder.build());
        }

        return formattedDirections.build();
    }

    private DocmosisDirection.Builder buildBaseDirection(Direction direction, int directionNumber) {
        return DocmosisDirection.builder()
            .assignee(direction.getAssignee())
            .title(formatTitle(directionNumber, direction))
            .body(direction.getDirectionText());
    }

    private boolean hasNewOtherAssignee(OtherPartiesDirectionAssignee other, Direction direction) {
        OtherPartiesDirectionAssignee assignee = direction.getOtherPartiesAssignee();
        return assignee != null && assignee != other;
    }

    private boolean hasNewRespondentAssignee(ParentsAndRespondentsDirectionAssignee respondent, Direction direction) {
        ParentsAndRespondentsDirectionAssignee assignee = direction.getParentsAndRespondentsAssignee();
        return assignee != null && assignee != respondent;
    }


    private String formatTitle(int index, Direction direction) {
        return String.format("%d. %s by %s", index, direction.getDirectionType(),
            ofNullable(direction.getDateToBeCompletedBy())
                .map(date -> formatLocalDateTimeBaseUsingFormat(date, "h:mma, d MMMM yyyy"))
                .orElse("unknown"));
    }

    private List<DocmosisRecital> buildRecitals(List<Element<Recital>> recitals) {
        return unwrapElements(recitals).stream()
            .filter(Objects::nonNull)
            .map(recital -> DocmosisRecital.builder()
                .title(defaultString(recital.getTitle(), DEFAULT))
                .body(defaultString(recital.getDescription(), DEFAULT))
                .build())
            .collect(toList());
    }
}
