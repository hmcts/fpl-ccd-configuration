package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.utils.PBANumberHelper.validatePBANumber;
import static uk.gov.hmcts.reform.fpl.utils.PBANumberHelper.updatePBANumber;

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

    public List<String> validatePBANumbers(CaseData caseData) {
        ImmutableList.Builder<String> errors = ImmutableList.builder();

        if (caseData.getApplicants() != null) {
            caseData.getApplicants().stream()
                .map(Element::getValue)
                .map(Applicant::getParty)
                .filter(Objects::nonNull)
                .forEach(applicantParty -> {
                    if (applicantParty.getPbaNumber() != null) {
                        errors.addAll(validatePBANumber(applicantParty.getPbaNumber()));
                    }
                });

        } else if (caseData.getApplicant() != null && caseData.getApplicant().getPbaNumber() != null) {
            errors.addAll(validatePBANumber(caseData.getApplicant().getPbaNumber()));
        }

        return errors.build();
    }


    public CaseDetails updatePBANumbers (CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (caseData.getApplicants() != null) {
            List<Element<Applicant>> applicants = caseData.getApplicants().stream()
                .map(element -> {
                    Applicant.ApplicantBuilder applicantBuilder = Applicant.builder();

                    if (element.getValue().getParty().getPbaNumber() != null) {
                        String pba = updatePBANumber(element.getValue().getParty().getPbaNumber());
                        applicantBuilder.party(element.getValue().getParty().toBuilder().pbaNumber(pba).build());
                    }

                    return Element.<Applicant>builder()
                        .id(element.getId())
                        .value(applicantBuilder.build())
                        .build();
                })
                .collect(Collectors.toList());

            caseDetails.getData().put("applicants", applicants);
        } else if (caseData.getApplicant() != null && caseData.getApplicant().getPbaNumber() != null) {
            String oldApplicationPBANumber = caseData.getApplicant().getPbaNumber();
            caseData.getApplicant().setPbaNumber(updatePBANumber(oldApplicationPBANumber));
            caseDetails.getData().put("applicant", caseData.getApplicant());
        }

        return caseDetails;
    }

    @SuppressWarnings("unchecked")
    public AboutToStartOrSubmitCallbackResponse addHiddenValues(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();

        if (caseDetails.getData().containsKey("applicants")) {
            List<Map<String, Object>> applicantParties = (List<Map<String, Object>>) data.get("applicants");

            List<ApplicantParty> applicantPartyList = applicantParties.stream()
                .map(entry -> mapper.convertValue(entry.get("value"), Map.class))
                .map(map -> mapper.convertValue(map.get("party"), ApplicantParty.class))
                .map(applicant -> {
                    ApplicantParty.ApplicantPartyBuilder partyBuilder = applicant.toBuilder();

                    //Variable within CCD part structure must be set to expand Collection.
                    //partyId and partyType are hidden fields so setting a value will not persist in database.
                    if (applicant.getPartyId() == null) {
                        partyBuilder.partyId(UUID.randomUUID().toString());
                        partyBuilder.partyType(PartyType.ORGANISATION);
                    }

                    return partyBuilder.build();
                })
                .collect(toList());

            List<Map<String, Object>> applicants = applicantPartyList.stream()
                .map(item -> ImmutableMap.<String, Object>builder()
                    .put("id", UUID.randomUUID().toString())
                    .put("value", ImmutableMap.of(
                        "party", mapper.convertValue(item, Map.class),
                        "leadApplicantIndicator", "Yes"))
                    .build())
                .collect(toList());

            data.put("applicants", applicants);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }
}
