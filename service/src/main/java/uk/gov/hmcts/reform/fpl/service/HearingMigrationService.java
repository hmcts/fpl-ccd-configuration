package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Hearing;
import uk.gov.hmcts.reform.fpl.utils.DateUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class HearingMigrationService {

    private static ObjectMapper MAPPER;

    static {
        // ensure only this service ignores null fields, rather than change the global objectmapper
        MAPPER = new ObjectMapper();
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

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
            Hearing hearing = MAPPER.convertValue(data.get("hearing1"), Hearing.class);
            Hearing.HearingBuilder hearingBuilder = hearing.toBuilder();

            if (hearing.getHearingID() == null || hearing.getHearingID().isBlank()) {
                String now = DateUtils.convertLocalDateTimeToString(LocalDateTime.now());
                hearingBuilder.hearingID(UUID.randomUUID().toString());
                hearingBuilder.hearingDate(now);
                hearingBuilder.createdBy("");
                hearingBuilder.createdDate(now);
            }

            data.put("hearing1", hearingBuilder.build());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

}
