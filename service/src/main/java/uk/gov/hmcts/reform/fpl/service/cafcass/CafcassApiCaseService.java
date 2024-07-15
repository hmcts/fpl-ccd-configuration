package uk.gov.hmcts.reform.fpl.service.cafcass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiAddress;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiApplicant;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiCase;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiChild;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiColleague;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiOther;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiRespondent;
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
            .caseManagementLocation(caseData.getCaseManagementLocation())
            .citizenIsApplicant(NO.equals(caseData.getIsLocalAuthority()))
            .applicantLA(caseData.getCaseLocalAuthority())
            .respondentLA(caseData.getRelatingLA())
            .applicants(getCafcassApiApplicant(caseData))
            .respondents(getCafcassApiRespondents(caseData))
            .children(getCafcassApiChild(caseData))
            .others(getCafcassApiOthers(caseData))
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
                    .designated(equalsIgnoreCase(applicant.getDesignated(), YES.getValue()))
                    .colleagues(applicant.getColleagues().stream()
                        .map(Element::getValue)
                        .map(colleague -> CafcassApiColleague.builder()
                            .role(colleague.getRole().toString())
                            .title(colleague.getTitle())
                            .email(colleague.getEmail())
                            .phone(colleague.getPhone())
                            .fullName(colleague.getFullName())
                            .mainContact(YES.equals(YesNo.valueOf(colleague.getMainContact())))
                            .notificationRecipient(YES.equals(YesNo.valueOf(colleague.getNotificationRecipient())))
                            .build())
                        .toList())
                    .build();
            })
            .toList();
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
                    .addressKnown(YES.equals(YesNo.valueOf(respondentParty.getAddressKnow())))
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
                    .detailsHidden(equalsIgnoreCase(childParty.getDetailsHidden(), YES.getValue()))
                    .socialWorkerName(childParty.getSocialWorkerName())
                    .socialWorkerTelephoneNumber(getTelephoneNumber(childParty.getSocialWorkerTelephoneNumber()))
                    .additionalNeeds(equalsIgnoreCase(childParty.getAdditionalNeeds(), YES.getValue()))
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
                    .addressKnown(equalsIgnoreCase(other.getAddressKnow(), YES.getValue()))
                    .addressUnknownReason(other.getAddressNotKnowReason())
                    .address(getCafcassApiAddress(other.getAddress()))
                    .telephone(other.getTelephone())
                    .litigationIssues(other.getLitigationIssues())
                    .litigationIssuesDetails(other.getLitigationIssuesDetails())
                    .detailsHidden(equalsIgnoreCase(other.getDetailsHidden(), YES.getValue()))
                    .detailsHiddenReason(other.getDetailsHiddenReason())
                    .build())
                .toList()
            )
            .orElse(List.of());
    }
}
