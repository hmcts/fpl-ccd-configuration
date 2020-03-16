package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.rd.model.ContactInformation;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
public class ApplicantService {

    public List<Element<Applicant>> expandApplicantCollection(CaseData caseData, Organisation organisation) {
        if (isEmpty(caseData.getApplicants())) {
            return ImmutableList.of(element(Applicant.builder()
                        .party(ApplicantParty.builder()
                            // A value within applicant party needs to be set in order to expand UI view.
                            .partyId(randomUUID().toString())
                            .organisationName(organisation.getName())
                            .address(getOrganisationAddress(organisation))
                        .build())
                    .build()));
        } else {
            return caseData.getApplicants();
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
                .map(element -> {
                    Applicant.ApplicantBuilder applicantBuilder = Applicant.builder();

                    if (isEmpty(element.getValue().getParty().getPartyId())) {
                        applicantBuilder.party(element.getValue().getParty().toBuilder()
                            .partyId(randomUUID().toString())
                            .partyType(PartyType.ORGANISATION).build());
                    } else {
                        applicantBuilder.party(element.getValue().getParty().toBuilder().build());
                    }

                    return Element.<Applicant>builder()
                        .id(element.getId())
                        .value(applicantBuilder.leadApplicantIndicator("Yes").build())
                        .build();
                })
                .collect(toList());
        }
        return applicants;
    }
}
