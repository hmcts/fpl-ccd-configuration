package uk.gov.hmcts.reform.fpl.service.cafcass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiAddress;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiApplicant;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiCase;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiColleague;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiRespondent;
import uk.gov.hmcts.reform.fpl.model.api.cafcass.CafcassApiSolicitor;
import uk.gov.hmcts.reform.fpl.model.cafcass.CafcassData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.cafcass.apibuilders.CafcassApiCaseDataConverter;
import uk.gov.hmcts.reform.fpl.service.search.SearchService;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.RangeQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

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
            .build();
    }

    private List<CafcassApiApplicant> getCafcassApiApplicant(CaseData caseData) {
        return caseData.getLocalAuthorities().stream()
            .map(applicantElement -> {
                LocalAuthority applicant = applicantElement.getValue();

                return CafcassApiApplicant.builder()
                    .id(applicantElement.getId().toString())
                    .name(applicant.getName())
                    .email(applicant.getEmail())
                    .phone(applicant.getPhone())
                    .address(getCafcassApiAddress(applicant.getAddress()))
                    .designated(YES.equals(YesNo.valueOf(applicant.getDesignated())))
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
        return caseData.getRespondents1().stream()
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

        CafcassApiSolicitor.CafcassApiSolicitorBuilder builder = CafcassApiSolicitor.builder()
            .email(respondentSolicitor.getEmail())
            .firstName(respondentSolicitor.getFirstName())
            .lastName(respondentSolicitor.getLastName());

        if (respondentSolicitor.getOrganisation() != null) {
            builder = builder.organisationId(respondentSolicitor.getOrganisation().getOrganisationID());
            builder = builder.organisationName(respondentSolicitor.getOrganisation().getOrganisationName());
        }

        return builder.build();
    }
}
