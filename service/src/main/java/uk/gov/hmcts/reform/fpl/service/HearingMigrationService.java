package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Hearing;

import java.sql.Date;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class HearingMigrationService {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

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

    public AboutToStartOrSubmitCallbackResponse addHiddenValues(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();

        if (caseDetails.getData().containsKey("hearing1")) {
            Hearing hearing = mapper.convertValue(data.get("hearing1"), Hearing.class);
            Hearing.HearingBuilder hearingBuilder = hearing.toBuilder();

            if (hearing.getHearingID() == null) {
                hearingBuilder.hearingID(UUID.randomUUID().toString());
                hearingBuilder.hearingDate(Date.from(ZonedDateTime.now().plusDays(1).toInstant()));
                hearingBuilder.createdBy("");
                hearingBuilder.createdDate(Date.from(ZonedDateTime.now().plusDays(1).toInstant()));
            }

            data.put("hearing1", hearingBuilder.build());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }
}
