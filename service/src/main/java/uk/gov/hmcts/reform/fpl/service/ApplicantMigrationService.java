package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Service
public class ApplicantMigrationService {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

//    public String setMigratedValue(CaseData caseData){
//        if(caseData.getApplicants() != null){
//            return "Yes";
//        } else {
//            return "No";
//        }
//    }
//
//    public List<Element<Applicant>> expandApplicantCollection(CaseData caseData) {
//        if (caseData.getApplicants() != null) {
//            List<Element<Applicant>> populatedApplicant = new ArrayList<>();
//
//            populatedApplicant.add(Element.<Applicant>builder()
//                .value(Applicant.builder()
//                    .party(ApplicantParty.builder()
//                        .partyId(UUID.randomUUID().toString())
//                        .build())
//                    .build())
//                .build());
//
//            return populatedApplicant;
//        } else {
//            return caseData.getApplicants();
//        }
//    }

    public AboutToStartOrSubmitCallbackResponse setMigratedValue(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();

        if (caseDetails.getData().containsKey("applicants") || !caseDetails.getData().containsKey("applicant")) {
            data.put("applicantsMigrated", "Yes");

            if (!caseDetails.getData().containsKey("applicants")) {
                List<Map<String, Object>> populatedApplicant = new ArrayList<>();
                populatedApplicant.add(ImmutableMap.of(
                    "id", UUID.randomUUID().toString(),
                    "value", ImmutableMap.of(
                        "party", ImmutableMap.of(
                            "partyId", UUID.randomUUID().toString()
                        )
                    ))
                );
                data.put("applicants", populatedApplicant);
            }
        } else {
            data.put("applicantsMigrated", "No");

        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
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
                        partyBuilder.partyType(PartyType.ORGANISATION.toString());
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
