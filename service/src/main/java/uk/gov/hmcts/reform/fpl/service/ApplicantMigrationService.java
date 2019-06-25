package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ApplicantMigrationService {

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

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(data)
                .build();
        } else {
            data.put("applicantsMigrated", "No");

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(data)
                .build();
        }
    }

}
