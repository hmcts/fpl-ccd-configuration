package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.Respondents;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.migration.MigratedRespondent;
import uk.gov.hmcts.reform.fpl.service.MapperService;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

@Api
@RestController
@RequestMapping("/callback/enter-respondents")
public class RespondentController {

    private final MapperService mapper;

    @Autowired
    public RespondentController(MapperService mapper) {
        this.mapper = mapper;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();

        System.out.println("data = " + data);

        if (caseDetails.getData().containsKey("respondents1")) {
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

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(callbackrequest.getCaseDetails().getData())
            .errors(validate(caseDetails))
            .build();
    }

    @SuppressWarnings("unchecked")
    private List<String> validate(CaseDetails caseDetails) {
        ImmutableList.Builder<String> errors = ImmutableList.builder();
        Map<String, Object> respondentsData = (Map<String, Object>) caseDetails.getData().get("respondents");

        if (caseDetails.getData().containsKey("respondents1")) {

            List<Map<String, Object>> migratedRespondentObject =
                (List<Map<String, Object>>) caseDetails.getData().get("respondents1");

            List<MigratedRespondent> migratedRespondents = migratedRespondentObject.stream()
                .map(respondent ->
                    mapper.mapObject((Map<String, Object>) respondent.get("value"), MigratedRespondent.class))
                .collect(toList());

            if (migratedRespondents.stream()
                .map(MigratedRespondent::getParty)
                .map(Party::getDateOfBirth)
                .filter(Objects::nonNull)
                .anyMatch(dob -> dob.after(new Date()))) {
                errors.add("Date of birth cannot be in the future");
            }
        }

        Respondents respondents = mapper.mapObject(respondentsData, Respondents.class);
        if (respondents.getAllRespondents().stream()
            .map(Respondent::getDob)
            .filter(Objects::nonNull)
            .anyMatch(dateOfBirth -> dateOfBirth.after(new Date()))) {
            errors.add("Date of birth cannot be in the future");
        }
        return errors.build();
    }
}
