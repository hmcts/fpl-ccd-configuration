package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

@Service
public class RespondentMigrationService {

    public AboutToStartOrSubmitCallbackResponse setMigratedValue(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();

        if (caseDetails.getData().containsKey("respondents1") || !caseDetails.getData().containsKey("respondents")) {
            data.put("respondentsMigrated", "Yes");

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(data)
                .build();
        } else {
            data.put("respondentsMigrated", "No");

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(data)
                .build();
        }
    }
}
