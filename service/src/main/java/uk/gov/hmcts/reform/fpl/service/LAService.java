package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.rd.model.ContactInformation;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.ObjectUtils.isEmpty;

@Service
@RequiredArgsConstructor
public class LAService {

    private final OrganisationService organisationService;

    public LocalAuthority getLocalAuthority(CaseData caseData) {
        if (isEmpty(caseData.getLocalAuthorities())) {
            Organisation organisation = organisationService.findOrganisation().orElse(Organisation.builder().build());

            return LocalAuthority.builder()
                .id(organisation.getOrganisationIdentifier())
                .name(organisation.getName())
                .address(getOrganisationAddress(organisation))
                .build();
        } else {
            return caseData.getLocalAuthorities().get(0).getValue();
        }
    }

    private Address getOrganisationAddress(Organisation organisation) {
        ContactInformation contactInformation = ContactInformation.builder().build();

        if (nonNull(organisation.getContactInformation())) {
            contactInformation = organisation.getContactInformation().get(0);
        }
        return contactInformation.toAddress();
    }

    public List<Element<Applicant>> addHiddenValues(CaseData caseData) {
        List<Element<Applicant>> applicants = new ArrayList<>();

        if (!isEmpty(caseData.getApplicants())) {
            applicants = caseData.getApplicants().stream()
                .map(applicantElement -> {
                    Applicant.ApplicantBuilder applicantBuilder = Applicant.builder();

                    if (isEmpty(applicantElement.getValue().getParty().getPartyId())) {
                        applicantBuilder.party(applicantElement.getValue().getParty().toBuilder()
                            .partyId(randomUUID().toString())
                            .partyType(PartyType.ORGANISATION).build());
                    } else {
                        applicantBuilder.party(applicantElement.getValue().getParty().toBuilder().build());
                    }

                    return Element.<Applicant>builder()
                        .id(applicantElement.getId())
                        .value(applicantBuilder.leadApplicantIndicator("Yes").build())
                        .build();
                })
                .collect(toList());
        }
        return applicants;
    }
}
