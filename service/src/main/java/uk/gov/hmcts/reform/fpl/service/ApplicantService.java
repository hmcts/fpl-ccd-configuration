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
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static net.logstash.logback.encoder.org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.springframework.util.ObjectUtils.isEmpty;

@Service
public class ApplicantService {

    public List<Element<Applicant>> expandApplicantCollection(CaseData caseData, Organisation organisation) {
        if (isEmpty(caseData.getApplicants())) {
            if (isEmpty(organisation)) {
                return ImmutableList.of(Element.<Applicant>builder()
                    .value(Applicant.builder()
                        .party(ApplicantParty.builder()
                            // A value within applicant party needs to be set in order to expand UI view.
                            .partyId(UUID.randomUUID().toString())
                            .build())
                        .build())
                    .build());
            }
            return buildApplicantWithOrganisationDetails(organisation);

        } else {
            return caseData.getApplicants();
        }
    }

    private List<Element<Applicant>> buildApplicantWithOrganisationDetails(Organisation organisation) {
        ContactInformation contactInformation;
        if (isNull(organisation.getContactInformation())) {
            contactInformation = ContactInformation.builder().build();
        } else {
            contactInformation = organisation.getContactInformation().get(0);
        }

        return ImmutableList.of(Element.<Applicant>builder()
            .value(Applicant.builder()
                .party(ApplicantParty.builder()
                    // A value within applicant party needs to be set in order to expand UI view.
                    .partyId(UUID.randomUUID().toString())
                    .organisationName(defaultIfNull(organisation.getName(), ""))
                    .address(buildApplicantAddressWithOrganisationDetails(contactInformation))
                    .build())
                .build())
            .build());
    }

    private Address buildApplicantAddressWithOrganisationDetails(ContactInformation contactInformation) {
        return Address.builder()
            .addressLine1(contactInformation.getAddressLine1())
            .addressLine2(contactInformation.getAddressLine2())
            .postTown(contactInformation.getPostCode())
            .country(contactInformation.getCounty())
            .build();
    }

    public List<Element<Applicant>> addHiddenValues(CaseData caseData) {
        List<Element<Applicant>> applicants = new ArrayList<>();

        if (!isEmpty(caseData.getApplicants())) {
            applicants = caseData.getApplicants().stream()
                .map(element -> {
                    Applicant.ApplicantBuilder applicantBuilder = Applicant.builder();

                    if (isEmpty(element.getValue().getParty().getPartyId())) {
                        applicantBuilder.party(element.getValue().getParty().toBuilder()
                            .partyId(UUID.randomUUID().toString())
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
