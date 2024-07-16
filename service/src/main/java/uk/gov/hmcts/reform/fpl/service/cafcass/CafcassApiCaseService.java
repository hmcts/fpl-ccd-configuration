package uk.gov.hmcts.reform.fpl.service.cafcass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.CaseLocation;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.FactorsParenting;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.InternationalElement;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Proceeding;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.Risks;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiAddress;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiApplicant;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiCase;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiCaseManagementLocation;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiChild;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiColleague;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiFactorsParenting;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiHearing;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiInternationalElement;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiOther;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiProceeding;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiRespondent;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiRisk;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.search.SearchService;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.RangeQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassApiCaseService {
    private final CaseConverter caseConverter;
    private final SearchService searchService;

    public List<CafcassApiCase> searchCaseByDateRange(LocalDateTime startDate, LocalDateTime endDate) {

        final RangeQuery searchRange = RangeQuery.builder()
            .field("last_modified")
            .greaterThanOrEqual(startDate)
            .lessThanOrEqual(endDate).build();

        List<CaseDetails> caseDetails = searchService.search(searchRange, 10000 , 0);

        return caseDetails.stream()
            .map(this::convertToCafcassApiCase)
            .toList();
    }

    private CafcassApiCase convertToCafcassApiCase(CaseDetails caseDetails) {
        return CafcassApiCase.builder()
            .caseId(caseDetails.getId())
            .jurisdiction(caseDetails.getJurisdiction())
            .state(caseDetails.getState())
            .caseTypeId(caseDetails.getCaseTypeId())
            .createdDate(caseDetails.getCreatedDate())
            .lastModified(caseDetails.getLastModified())
            .caseData(getCafcassApiCaseData(caseConverter.convert(caseDetails)))
            .build();
    }

    private CafcassApiCaseData getCafcassApiCaseData(CaseData caseData) {
        return CafcassApiCaseData.builder()
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .dateSubmitted(caseData.getDateSubmitted())
            .applicationType(caseData.isC1Application() ? "C1" : "C110A")
            .ordersSought(caseData.getOrders().getOrderType())
            .dateOfCourtIssue(caseData.getDateOfIssue())
            .citizenIsApplicant(NO.equals(caseData.getIsLocalAuthority()))
            .applicantLA(caseData.getCaseLocalAuthority())
            .respondentLA(caseData.getRelatingLA())
            .applicants(getCafcassApiApplicant(caseData))
            .respondents(getCafcassApiRespondents(caseData))
            .children(getCafcassApiChild(caseData))
            .others(getCafcassApiOthers(caseData))
            .hearingDetails(getCafcassApiHearing(caseData))
            .internationalElement(getCafcassApiInternationalElement(caseData))
            .previousProceedings(getCafcassApiProceeding(caseData))
            .risks(getCafcassApiRisk(caseData))
            .factorsParenting(getCafcassApiFactorsParenting(caseData))
            .caseManagementLocation(getCafcassApiCaseManagementLocation(caseData))
            .build();
    }

    private List<CafcassApiApplicant> getCafcassApiApplicant(CaseData caseData) {
        return Optional.ofNullable(caseData.getLocalAuthorities()).orElse(List.of()).stream()
            .map(applicantElement -> {
                LocalAuthority applicant = applicantElement.getValue();

                return CafcassApiApplicant.builder()
                    .id(applicantElement.getId().toString())
                    .name(applicant.getName())
                    .email(applicant.getEmail())
                    .phone(applicant.getPhone())
                    .address(getCafcassApiAddress(applicant.getAddress()))
                    .designated(isYes(applicant.getDesignated()))
                    .colleagues(applicant.getColleagues().stream()
                        .map(Element::getValue)
                        .map(colleague -> CafcassApiColleague.builder()
                            .role(colleague.getRole().toString())
                            .title(colleague.getTitle())
                            .email(colleague.getEmail())
                            .phone(colleague.getPhone())
                            .fullName(colleague.getFullName())
                            .mainContact(isYes(colleague.getMainContact()))
                            .notificationRecipient(isYes(colleague.getNotificationRecipient()))
                            .build())
                        .toList())
                    .build();
            })
            .toList();
    }

    private boolean isYes(String yesNo) {
        return YES.getValue().equalsIgnoreCase(yesNo);
    }

    private CafcassApiAddress getCafcassApiAddress(Address address) {
        return Optional.ofNullable(address)
            .map(add -> CafcassApiAddress.builder()
                .addressLine1(add.getAddressLine1())
                .addressLine2(add.getAddressLine2())
                .addressLine3(add.getAddressLine3())
                .postTown(add.getPostTown())
                .county(add.getCounty())
                .postcode(add.getPostcode())
                .country(add.getCountry())
                .build())
            .orElse(null);
    }

    private List<CafcassApiRespondent> getCafcassApiRespondents(CaseData caseData) {
        return Optional.ofNullable(caseData.getRespondents1()).orElse(List.of()).stream()
            .map(Element::getValue)
            .map(respondent -> {
                RespondentParty respondentParty = respondent.getParty();
                return CafcassApiRespondent.builder()
                    .firstName(respondentParty.getFirstName())
                    .lastName(respondentParty.getLastName())
                    .gender(respondentParty.getGender())
                    .addressKnown(isYes(respondentParty.getAddressKnow()))
                    .addressUnknownReason(respondentParty.getAddressNotKnowReason())
                    .address(getCafcassApiAddress(respondentParty.getAddress()))
                    .dateOfBirth(respondentParty.getDateOfBirth())
                    .telephoneNumber(getTelephoneNumber(respondentParty.getTelephoneNumber()))
                    .litigationIssues(respondentParty.getLitigationIssues())
                    .litigationIssuesDetails(respondentParty.getLitigationIssuesDetails())
                    .contactDetailsHidden(respondentParty.getContactDetailsHidden())
                    .contactDetailsHiddenReason(respondentParty.getContactDetailsHiddenReason())
                    .relationshipToChild(respondentParty.getRelationshipToChild())
                    .solicitor(getCafcassApiSolicitor(respondent.getSolicitor()))
                    .build();
            })
            .toList();
    }

    private String getTelephoneNumber(Telephone telephone) {
        return Optional.ofNullable(telephone)
            .map(Telephone::getTelephoneNumber)
            .orElse(null);
    }

    private CafcassApiSolicitor getCafcassApiSolicitor(RespondentSolicitor respondentSolicitor) {
        CafcassApiSolicitor.CafcassApiSolicitorBuilder builder = CafcassApiSolicitor.builder();
        if (respondentSolicitor != null) {
            builder = builder.email(respondentSolicitor.getEmail());
            builder = builder.firstName(respondentSolicitor.getFirstName());
            builder = builder.lastName(respondentSolicitor.getLastName());

            if (respondentSolicitor.getOrganisation() != null) {
                builder = builder.organisationId(respondentSolicitor.getOrganisation().getOrganisationID());
                builder = builder.organisationName(respondentSolicitor.getOrganisation().getOrganisationName());
            }
        }

        return builder.build();
    }

    private List<CafcassApiChild> getCafcassApiChild(CaseData caseData) {
        return Optional.ofNullable(caseData.getChildren1()).orElse(List.of()).stream()
            .map(Element::getValue)
            .map(child -> {
                ChildParty childParty = child.getParty();
                return CafcassApiChild.builder()
                    .firstName(childParty.getFirstName())
                    .lastName(childParty.getLastName())
                    .dateOfBirth(childParty.getDateOfBirth())
                    .gender(childParty.getGender().toString())
                    .genderIdentification(childParty.getGenderIdentification())
                    .livingSituation(childParty.getLivingSituation())
                    .livingSituationDetails(childParty.getLivingSituationDetails())
                    .address(getCafcassApiAddress(childParty.getAddress()))
                    .careAndContactPlan(childParty.getCareAndContactPlan())
                    .detailsHidden(isYes(childParty.getDetailsHidden()))
                    .socialWorkerName(childParty.getSocialWorkerName())
                    .socialWorkerTelephoneNumber(getTelephoneNumber(childParty.getSocialWorkerTelephoneNumber()))
                    .additionalNeeds(isYes(childParty.getAdditionalNeeds()))
                    .additionalNeedsDetails(childParty.getAdditionalNeedsDetails())
                    .litigationIssues(childParty.getLitigationIssues())
                    .litigationIssuesDetails(childParty.getLitigationIssuesDetails())
                    .solicitor(getCafcassApiSolicitor(child.getSolicitor()))
                    .fathersResponsibility(childParty.getFathersResponsibility())
                    .build();
            })
            .toList();
    }

    private List<CafcassApiOther> getCafcassApiOthers(CaseData caseData) {
        return Optional.ofNullable(caseData.getOthers())
            .map(others -> Stream.concat(Optional.ofNullable(others.getFirstOther()).stream(),
                    unwrapElements(others.getAdditionalOthers()).stream())
                .filter(Objects::nonNull)
                .map(other -> CafcassApiOther.builder()
                    .name(other.getName())
                    .dateOfBirth(other.getDateOfBirth())
                    .gender(other.getGender())
                    .genderIdentification(other.getGenderIdentification())
                    .birthPlace(other.getBirthPlace())
                    .addressKnown(isYes(other.getAddressKnow()))
                    .addressUnknownReason(other.getAddressNotKnowReason())
                    .address(getCafcassApiAddress(other.getAddress()))
                    .telephone(other.getTelephone())
                    .litigationIssues(other.getLitigationIssues())
                    .litigationIssuesDetails(other.getLitigationIssuesDetails())
                    .detailsHidden(isYes(other.getDetailsHidden()))
                    .detailsHiddenReason(other.getDetailsHiddenReason())
                    .build())
                .toList()
            )
            .orElse(List.of());
    }

    private List<CafcassApiHearing> getCafcassApiHearing(CaseData caseData) {
        return Optional.ofNullable(caseData.getHearingDetails()).orElse(List.of()).stream()
            .map(hearingBookingElement -> {
                HearingBooking hearingBooking = hearingBookingElement.getValue();
                return CafcassApiHearing.builder()
                    .id(hearingBookingElement.getId().toString())
                    .type(hearingBooking.getType())
                    .typeDetails(hearingBooking.getTypeDetails())
                    .venue(hearingBooking.getVenue())
                    .status(hearingBooking.getStatus())
                    .startDate(hearingBooking.getStartDate())
                    .endDate(hearingBooking.getEndDate())
                    .attendance(hearingBooking.getAttendance())
                    .cancellationReason(hearingBooking.getCancellationReason())
                    .preAttendanceDetails(hearingBooking.getPreAttendanceDetails())
                    .attendanceDetails(hearingBooking.getAttendanceDetails())
                    .build();
            })
            .toList();
    }

    private CafcassApiInternationalElement getCafcassApiInternationalElement(CaseData caseData) {
        CafcassApiInternationalElement.CafcassApiInternationalElementBuilder builder =
            CafcassApiInternationalElement.builder();

        final InternationalElement internationalElement = caseData.getInternationalElement();
        if (internationalElement != null) {
            builder = builder.possibleCarer(isYes(internationalElement.getPossibleCarer()))
                .possibleCarerReason(internationalElement.getPossibleCarerReason())
                .significantEvents(isYes(internationalElement.getSignificantEvents()))
                .significantEventsReason(internationalElement.getSignificantEventsReason())
                .issues(isYes(internationalElement.getIssues()))
                .issuesReason(internationalElement.getIssuesReason())
                .proceedings(isYes(internationalElement.getProceedings()))
                .proceedingsReason(internationalElement.getProceedingsReason())
                .internationalAuthorityInvolvement(isYes(internationalElement.getInternationalAuthorityInvolvement()))
                .internationalAuthorityInvolvementDetails(internationalElement
                    .getInternationalAuthorityInvolvementDetails());
        }

        return builder.build();
    }

    private List<CafcassApiProceeding> getCafcassApiProceeding(CaseData caseData) {
        return caseData.getAllProceedings().stream()
            .map(Element::getValue)
            .map(proceeding -> CafcassApiProceeding.builder()
                .proceedingStatus(proceeding.getProceedingStatus())
                .caseNumber(proceeding.getCaseNumber())
                .started(proceeding.getProceedingStatus())
                .ended(proceeding.getEnded())
                .ordersMade(proceeding.getOrdersMade())
                .judge(proceeding.getJudge())
                .children(proceeding.getChildren())
                .guardian(proceeding.getGuardian())
                .sameGuardianNeeded(isYes(proceeding.getSameGuardianNeeded()))
                .sameGuardianDetails(proceeding.getSameGuardianDetails())
                .build())
            .toList();
    }

    private CafcassApiRisk getCafcassApiRisk(CaseData caseData) {
        CafcassApiRisk.CafcassApiRiskBuilder builder = CafcassApiRisk.builder();

        Risks risk = caseData.getRisks();
        if (risk != null) {
            builder = builder
                .neglectOccurrences(risk.getNeglectOccurrences())
                .sexualAbuseOccurrences(risk.getSexualAbuseOccurrences())
                .physicalHarmOccurrences(risk.getPhysicalHarmOccurrences())
                .emotionalHarmOccurrences(risk.getEmotionalHarmOccurrences());
        }
        return builder.build();
    }

    private CafcassApiFactorsParenting getCafcassApiFactorsParenting(CaseData caseData) {
        CafcassApiFactorsParenting.CafcassApiFactorsParentingBuilder builder = CafcassApiFactorsParenting.builder();

        FactorsParenting factorsParenting = caseData.getFactorsParenting();
        if (factorsParenting != null) {
            builder = builder.alcoholDrugAbuse(isYes(factorsParenting.getAlcoholDrugAbuse()))
                .alcoholDrugAbuseReason(factorsParenting.getAlcoholDrugAbuseReason())
                .domesticViolence(isYes(factorsParenting.getDomesticViolence()))
                .domesticViolenceReason(factorsParenting.getDomesticViolenceReason())
                .anythingElse(isYes(factorsParenting.getAnythingElse()))
                .anythingElseReason(factorsParenting.getAnythingElseReason());
        }
        return builder.build();
    }

    private CafcassApiCaseManagementLocation getCafcassApiCaseManagementLocation(CaseData caseData) {
        CafcassApiCaseManagementLocation.CafcassApiCaseManagementLocationBuilder builder =
            CafcassApiCaseManagementLocation.builder();

        CaseLocation caseLocation = caseData.getCaseManagementLocation();
        if (caseLocation != null) {
            builder = builder.region(caseLocation.getRegion()).baseLocation(caseLocation.getBaseLocation());
        }
        return builder.build();
    }
}
