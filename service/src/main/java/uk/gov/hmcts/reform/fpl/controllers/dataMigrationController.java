package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.fpl.service.MapperService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Api
@RestController
@RequestMapping("callback/migration")
public class dataMigrationController {
    private final MapperService mapperService;
    private ObjectMapper oMapper = new ObjectMapper();

    @Autowired
    public dataMigrationController(MapperService mapperService) {
        this.mapperService = mapperService;
    }

    @PostMapping("/submitted")
    @SuppressWarnings("unchecked")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        // Get case data
        Map<String, Object> data = caseDetails.getData();

        Map<String, Object> respondents = oMapper.convertValue(data.get("respondents"), Map.class);

        Map<String, Object> firstRespondent = oMapper.convertValue(respondents.get("firstRespondent"), Map.class);

        Map<String, Object> transformedFirstRespondent = new HashMap<String, Object>();

        firstRespondent.put("id", "12345");

        // Reformat name
        firstRespondent.putAll(GetFullName(firstRespondent.get("name").toString()));
        firstRespondent.remove("name");

        // Reformat DOB
        firstRespondent.put("dateOfBirth", firstRespondent.get("dob").toString());
        firstRespondent.remove("dob");

        // Reformat Telephone
        String tempTelephone = firstRespondent.get("telephone").toString();
        firstRespondent.remove("telephone");
        firstRespondent.put("telephone", ImmutableMap.builder()
            .put("telephone", tempTelephone)
            .build());

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
            value.put("telephone", ImmutableMap.builder()
                .put("telephone", tempRespondentTelephone)
                .build());

            return respondent;
        }).collect(Collectors.toList());

        // Adds first respondent to array
        migratedRespondentCollection.add(0, transformedFirstRespondent);

        data.remove("respondents");
        data.put("migrated", "Yes");
        data.put("respondents", migratedRespondentCollection);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    private Map<String, Object> GetFullName(String name)  {
        String[] names = name.trim().split("\\s+");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("firstName", names[0]);
        map.put("lastName", names[1]);
        return map;
    }
}
