package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

@Service
public class HearingMigrationService {

    public AboutToStartOrSubmitCallbackResponse setMigratedValue(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();

        if (caseDetails.getData().containsKey("hearing1") || !caseDetails.getData().containsKey("hearing")) {
            data.put("hearingMigrated", "Yes");
        } else {
            data.put("hearingMigrated", "No");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

}
