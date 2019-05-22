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

import java.util.HashMap;
import java.util.Map;

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

        // Orginal applicant
        Map<String, Object> applicant = oMapper.convertValue(data.get("applicant"), Map.class);

        // Init applicant party map
        Map<String, Object> party = new HashMap<String, Object>();

        // Build new applicant party object
        party.putAll(GetFullName(applicant.get("name").toString()));
        party.put("partyType", "Indivdual");
        party.put("address", oMapper.convertValue(applicant.get("address"), Map.class));
        party.put("email", ImmutableMap.builder()
            .put("email", applicant.get("email"))
            .build());
        party.put("telephone", ImmutableMap.builder()
            .put("telephone", applicant.get("telephone"))
            .put("mobile", applicant.get("mobile"))
            .build());

        // Misc applicant data
        party.put("jobTitle", applicant.get("jobTitle"));
        party.put("personToContact", applicant.get("personToContact"));
        party.put("isMigrated", true);

        // Appending new applicants object to case data
        data.put("applicants", ImmutableList.builder()
            .add(party)
            .build());

        // Remove orginal applicant key
        data.remove("applicant");

        System.out.println(data);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    private Map<String, Object> GetFullName(String name)  {
        String[] names = name.trim().split("\\s+");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("firstName", names[0]);
        map.put("secondName", names[1]);
        return map;
    }
}
