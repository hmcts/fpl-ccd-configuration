package uk.gov.hmcts.reform.fpl.service.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.OtherPartiesDirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.ParentsAndRespondentsDirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.Recital;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisDirection;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudge;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRecital;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRepresentative;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRepresentedBy;
import uk.gov.hmcts.reform.fpl.model.interfaces.Representable;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

/**
 * Service to generate docmosis objects that can be sent to
 * {@link uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates#CMO the CMO template}.
 *
 * @deprecated to be removed with {@link CaseManagementOrder}
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Deprecated(since = "FPLA-1915")
public class CaseManagementOrderGenerationService extends DocmosisTemplateDataGeneration<DocmosisCaseManagementOrder> {
    private static final String HEARING_EMPTY_PLACEHOLDER = "This will appear on the issued CMO";
    private static final String DEFAULT = EMPTY;

    private final HearingBookingService hearingService;
    private final CaseDataExtractionService dataExtractionService;

    public DocmosisCaseManagementOrder getTemplateData(CaseData caseData) {
        List<Element<HearingBooking>> hearingDetails = caseData.getHearingDetails();
        CaseManagementOrder caseManagementOrder = caseData.getCaseManagementOrder();

        HearingBooking nextHearing = null;

        if (needsNextHearingDate(caseManagementOrder)) {
            UUID nextHearingId = caseManagementOrder.getNextHearing().getId();
            nextHearing = hearingService.getHearingBookingByUUID(hearingDetails, nextHearingId);
        }

        HearingBooking hearingBooking = hearingService
            .getHearingBookingByUUID(hearingDetails, caseManagementOrder.getId());

        DocmosisCaseManagementOrder.DocmosisCaseManagementOrderBuilder<?, ?> order
            = DocmosisCaseManagementOrder.builder()
            .judgeAndLegalAdvisor(getJudgeAndLegalAdvisor(hearingBooking))
            .allocatedJudge(getAllocatedJudge(caseData.getAllocatedJudge()))
            .courtName(dataExtractionService.getCourtName(caseData.getCaseLocalAuthority()))
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .dateOfIssue(caseManagementOrder.getDateOfIssue())
            .complianceDeadline(caseData.getComplianceDeadline())
            .children(dataExtractionService.getChildrenDetails(caseData.getAllChildren()))
            .respondents(dataExtractionService.getRespondentsNameAndRelationship(caseData.getAllRespondents()))
            .respondentsProvided(isNotEmpty(caseData.getAllRespondents()))
            .applicantName(dataExtractionService.getApplicantName(caseData.getAllApplicants()))
            .directions(buildDocmosisDirections(caseManagementOrder.getDirections()))
            .hearingBooking(dataExtractionService.getHearingBookingData(nextHearing, HEARING_EMPTY_PLACEHOLDER))
            .representatives(getRepresentatives(caseData))
            .recitals(buildRecitals(caseManagementOrder.getRecitals()))
            .recitalsProvided(isNotEmpty(buildRecitals(caseManagementOrder.getRecitals())))
            .schedule(caseManagementOrder.getSchedule())
            .scheduleProvided("Yes".equals(getScheduleProvided(caseManagementOrder)))
            .crest(getCrestData());

        if (caseManagementOrder.isDraft()) {
            order.draftbackground(getDraftWaterMarkData());
        } else {
            order.courtseal(getCourtSealData());
        }

        return order.build();
    }

    private DocmosisJudgeAndLegalAdvisor getJudgeAndLegalAdvisor(HearingBooking hearingBooking) {
        return dataExtractionService.getJudgeAndLegalAdvisor(hearingBooking.getJudgeAndLegalAdvisor());
    }

    private DocmosisJudge getAllocatedJudge(Judge allocatedJudge) {
        return dataExtractionService.getAllocatedJudge(JudgeAndLegalAdvisor.from(allocatedJudge));
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

    private List<DocmosisDirection> buildDocmosisDirections(List<Element<Direction>> elements) {
        List<Direction> directions = unwrapElements(elements);
        List<DocmosisDirection> formattedDirections = new ArrayList<>();
        ParentsAndRespondentsDirectionAssignee respondentHeader = null;
        OtherPartiesDirectionAssignee otherHeader = null;
        int directionNumber = 2;

        for (Direction direction : directions) {
            DocmosisDirection.Builder builder = dataExtractionService.baseDirection(direction, directionNumber++);

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
        return formattedDirections;
    }

    private boolean hasNewOtherAssignee(OtherPartiesDirectionAssignee other, Direction direction) {
        OtherPartiesDirectionAssignee assignee = direction.getOtherPartiesAssignee();
        return assignee != null && assignee != other;
    }

    private boolean hasNewRespondentAssignee(ParentsAndRespondentsDirectionAssignee respondent, Direction direction) {
        ParentsAndRespondentsDirectionAssignee assignee = direction.getParentsAndRespondentsAssignee();
        return assignee != null && assignee != respondent;
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
