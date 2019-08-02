package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Service
public class ApplicantMigrationService {

    public String setMigratedValue(CaseData caseData) {
        if (caseData.getApplicants() != null || caseData.getApplicant() == null) {
            return "Yes";
        } else {
            return "No";
        }
    }

    public List<Element<Applicant>> expandApplicantCollection(CaseData caseData) {
        if (caseData.getApplicants() == null) {
            List<Element<Applicant>> populatedApplicant = new ArrayList<>();

            populatedApplicant.add(Element.<Applicant>builder()
                .value(Applicant.builder()
                    .party(ApplicantParty.builder()
                        .partyId(UUID.randomUUID().toString())
                        .build())
                    .build())
                .build());

            return populatedApplicant;
        } else {
            return caseData.getApplicants();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Element<Applicant>> addHiddenValues(CaseData caseData) {
        List<Element<Applicant>> applicants = new ArrayList<>();

        if (caseData.getApplicants() != null) {
            applicants = caseData.getApplicants().stream()
                .map(element -> {
                    Applicant.ApplicantBuilder applicantBuilder = Applicant.builder();

                    if (element.getValue().getParty().getPartyId() == null) {
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
