package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Recital;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisDirection;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRecital;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRepresentative;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRepresentedBy;
import uk.gov.hmcts.reform.fpl.model.interfaces.Representable;

import java.io.IOException;
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
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseManagementOrderGenerationService extends DocmosisTemplateDataGeneration<DocmosisCaseManagementOrder> {
    private static final String HEARING_EMPTY_PLACEHOLDER = "This will appear on the issued CMO";

    private final HearingBookingService hearingBookingService;
    private final CommonCaseDataExtractionService dataExtractionService;
    private final DraftCMOService cmoService;

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
            .judgeAndLegalAdvisor(getJudgeAndLegalAdvisor(hearingBooking))
            .courtName(dataExtractionService.getCourtName(caseData.getCaseLocalAuthority()))
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .dateOfIssue(caseManagementOrder.getDateOfIssue())
            .complianceDeadline(caseData.getComplianceDeadline())
            .children(dataExtractionService.getChildrenDetails(caseData.getAllChildren()))
            .respondents(dataExtractionService.getRespondentsNameAndRelationship(caseData.getAllRespondents()))
            .respondentsProvided(isNotEmpty(caseData.getAllRespondents()))
            .applicantName(dataExtractionService.getApplicantName(caseData.getAllApplicants()))
            .directions(getGroupedCMODirections(caseManagementOrder))
            .hearingBooking(dataExtractionService.getHearingBookingData(nextHearing, HEARING_EMPTY_PLACEHOLDER))
            .representatives(getRepresentatives(caseData))
            .recitals(buildRecitals(caseManagementOrder.getRecitals()))
            .recitalsProvided(isNotEmpty(buildRecitals(caseManagementOrder.getRecitals())))
            .schedule(caseManagementOrder.getSchedule())
            .scheduleProvided("Yes".equals(getScheduleProvided(caseManagementOrder)));

        if (caseManagementOrder.isDraft()) {
            order.draftbackground(format(BASE_64, generateDraftWatermarkEncodedString()));
        }

        if (caseManagementOrder.isSealed()) {
            order.courtseal(format(BASE_64, generateCourtSealEncodedString()));
        }

        return order.build();
    }

    private DocmosisJudgeAndLegalAdvisor getJudgeAndLegalAdvisor(HearingBooking hearingBooking) {
        return dataExtractionService.getJudgeAndLegalAdvisor(hearingBooking.getJudgeAndLegalAdvisor());
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

    private List<DocmosisDirection> getGroupedCMODirections(CaseManagementOrder caseManagementOrder) {
        return ofNullable(caseManagementOrder.getDirections()).map(directions -> {
                ImmutableList.Builder<DocmosisDirection> formattedDirections = ImmutableList.builder();

                int directionNumber = 1;
                for (Element<Direction> element : directions) {
                    Direction direction = element.getValue();
                    if (direction.getParentsAndRespondentsAssignee() != null) {
                        formattedDirections.add(DocmosisDirection.builder()
                            .header("For " + direction.getParentsAndRespondentsAssignee().getLabel())
                            .assignee(direction.getAssignee())
                            .title(formatTitle(directionNumber++, direction))
                            .body(direction.getDirectionText())
                            .build());
                    }

                    if (direction.getOtherPartiesAssignee() != null) {
                        formattedDirections.add(DocmosisDirection.builder()
                            .header("For " + direction.getOtherPartiesAssignee().getLabel())
                            .assignee(direction.getAssignee())
                            .title(formatTitle(directionNumber++, direction))
                            .body(direction.getDirectionText())
                            .build());

                    } else {
                        formattedDirections.add(DocmosisDirection.builder()
                            .assignee(direction.getAssignee())
                            .title(formatTitle(directionNumber++, direction))
                            .body(direction.getDirectionText())
                            .build());
                    }
                }

                return formattedDirections.build();
            }
        ).orElse(ImmutableList.of());
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
