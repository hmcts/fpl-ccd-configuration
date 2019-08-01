package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OldApplicant;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.PBANumberHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.utils.PBANumberHelper.updatePBANumber;
import static uk.gov.hmcts.reform.fpl.utils.PBANumberHelper.validatePBANumber;

@Service
public class ApplicantMigrationService {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

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

    public AboutToStartOrSubmitCallbackResponse validateAndUpdatePBANumbers(CaseDetails caseDetails) {
        ImmutableList.Builder<String> validationErrors = ImmutableList.builder();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (caseData.getApplicants() != null) {
            List<Element<Applicant>> applicants = caseData.getApplicants().stream()
                .map(element -> {
                    Applicant.ApplicantBuilder applicantBuilder = Applicant.builder();

                    if (element.getValue().getParty().getPbaNumber() != null) {
                        String pba = updatePBANumber(element.getValue().getParty().getPbaNumber());
                        validationErrors.addAll(validatePBANumber(pba));

                        applicantBuilder.party(element.getValue().getParty().toBuilder().pbaNumber(pba).build());
                    }

                    return Element.<Applicant>builder()
                        .id(element.getId())
                        .value(applicantBuilder.build())
                        .build();
                })
                .collect(Collectors.toList());

            caseDetails.getData().put("applicants", applicants);

        } else {
            OldApplicant applicantData = caseData.getApplicant();

            if (isNullOrEmpty(applicantData.getPbaNumber())) {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(caseDetails.getData())
                    .errors(validationErrors.build())
                    .build();
            }

            String newPbaNumberData = PBANumberHelper.updatePBANumber(applicantData.getPbaNumber());
            validationErrors.addAll(validatePBANumber(newPbaNumberData));

            if (validationErrors.build().isEmpty()) {
                applicantData.setPbaNumber(newPbaNumberData);
                caseDetails.getData().put("applicant", applicantData);
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(validationErrors.build())
            .build();
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
