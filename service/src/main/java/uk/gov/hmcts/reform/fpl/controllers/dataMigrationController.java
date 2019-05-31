package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fpl.service.MapperService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Api
@RestController
@RequestMapping("callback/migration")
public class dataMigrationController {
    private final MapperService mapperService;
    private ObjectMapper oMapper = new ObjectMapper();
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;

    @Autowired
    public dataMigrationController(
        MapperService mapperService,
        AuthTokenGenerator authTokenGenerator,
        CoreCaseDataApi coreCaseDataApi
    ) {
        this.mapperService = mapperService;
        this.authTokenGenerator = authTokenGenerator;
        this.coreCaseDataApi = coreCaseDataApi;
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

        Map<String, Object> respondents = oMapper.convertValue(data.get("respondents"), Map.class);

        Map<String, Object> firstRespondent = oMapper.convertValue(respondents.get("firstRespondent"), Map.class);

        Map<String, Object> transformedFirstRespondent = new HashMap<String, Object>();

        // Reformat name
        firstRespondent.putAll(GetFullName(firstRespondent.get("name").toString()));
        firstRespondent.remove("name");

        // Reformat DOB
        firstRespondent.put("dateOfBirth", firstRespondent.get("dob").toString());
        firstRespondent.remove("dob");

        // Reformat Telephone
        String tempTelephone = firstRespondent.get("telephone").toString();
        firstRespondent.remove("telephone");
        firstRespondent.put("telephone", tempTelephone);

        transformedFirstRespondent.put("value", firstRespondent);
        transformedFirstRespondent.put("id", "12345");

        List<Map<String, Object>> additionalRespondents = (List<Map<String, Object>>) oMapper.convertValue(respondents.get("additional"), List.class);

        // additionalRespondents collection
        List<Map<String, Object>> migratedRespondentCollection = additionalRespondents.stream().map(respondent -> {

            // Reference to value
            Map<String, Object> value = oMapper.convertValue(respondent.get("value"), Map.class);

            // Reformat name
            value.putAll(GetFullName(value.get("name").toString()));
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

    private Map<String, Object> GetFullName(String name)  {
        String[] names = name.trim().split("\\s+");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("firstName", names[0]);
        map.put("lastName", names[1]);
        return map;
    }
}
