package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.service.MigrationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Api
@RestController
@RequestMapping("callback/migration")
class DataMigrationController {
    private ObjectMapper objectMapper = new ObjectMapper();
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;
    private final MigrationService migrationService;

    @Autowired
    public DataMigrationController(AuthTokenGenerator authTokenGenerator,
                                   CoreCaseDataApi coreCaseDataApi,
                                   MigrationService migrationService) {
        this.authTokenGenerator = authTokenGenerator;
        this.coreCaseDataApi = coreCaseDataApi;
        this.migrationService = migrationService;
    }

    @PostMapping("/submitted/{id}")
    @SuppressWarnings("unchecked")
    public void handleAboutToSubmitEvent(
        @PathVariable("id") String caseID,
        @RequestHeader(value = "user-id") String userID,
        @RequestHeader(value = "authorization") String authorization) {

        // Init service token
        String serviceToken = this.authTokenGenerator.generate();

        // Get caseDetails from CCD
        CaseDetails caseDetails = this.coreCaseDataApi.getCase(authorization, serviceToken, caseID);

        // Get case data
        Map<String, Object> data = caseDetails.getData();

        Map<String, Object> respondents = objectMapper.convertValue(data.get("respondents"), Map.class);

        Respondent firstRespondent = objectMapper.convertValue(respondents.get("firstRespondent"), Respondent.class);

        Map<String, Object> transformedFirstRespondent = ImmutableMap.of(
            "id", UUID.randomUUID().toString(),
            "value", migrationService.migrateRespondent(firstRespondent));

        List<Map<String, Object>> additionalRespondents =
            (List<Map<String, Object>>) objectMapper.convertValue(respondents.get("additional"), List.class);

        // additionalRespondents collection
        List<Map<String, Object>> migratedRespondentCollection = additionalRespondents.stream().map(respondent -> {

            // Reference to value
            Map<String, Object> value = objectMapper.convertValue(respondent.get("value"), Map.class);

            // Reformat name
            value.putAll(getFullName(value.get("name").toString()));
            value.remove("name");

            // Reformat DOB
            value.put("dateOfBirth", value.get("dob").toString());
            value.remove("dob");

            // Reformat Telephone
            String tempRespondentTelephone = value.get("telephone").toString();
            value.remove("telephone");
            value.put("telephone", tempRespondentTelephone);

            return respondent;
        }).collect(Collectors.toList());

        // Adds first respondent to array
        migratedRespondentCollection.add(0, transformedFirstRespondent);

        data.remove("respondents");
        data.put("respondents1", migratedRespondentCollection);

        StartEventResponse startEventResponse = this.coreCaseDataApi
            .startEventForCaseWorker(
                authorization,
                serviceToken,
                userID,
                "PUBLICLAW",
                "CARE_SUPERVISION_EPO",
                caseID,
                "enterRespondentsNew"
            );

        CaseDataContent caseData = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .build()
            ).data(data)
            .build();

        CaseDetails updatedCaseDetails = this.coreCaseDataApi.submitEventForCaseWorker(
            authorization, serviceToken, userID, "PUBLICLAW",
            "CARE_SUPERVISION_EPO", caseID, true, caseData);

        System.out.println(updatedCaseDetails.getCallbackResponseStatus());
    }

    private Map<String, Object> getFullName(String name) {
        String[] names = name.trim().split("\\s+");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("firstName", names[0]);
        map.put("lastName", names[1]);
        return map;
    }
}
