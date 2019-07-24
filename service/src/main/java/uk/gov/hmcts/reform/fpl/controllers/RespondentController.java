package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.fpl.model.common.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.migration.MigratedRespondent;
import uk.gov.hmcts.reform.fpl.service.RespondentService;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Api
@RestController
@RequestMapping("/callback/enter-respondents")
public class RespondentController {

    private final ObjectMapper mapper;
    private final RespondentService respondentService;

    @Autowired
    public RespondentController(ObjectMapper mapper,
                                RespondentService respondentService) {
        this.mapper = mapper;
        this.respondentService = respondentService;
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        CaseData alteredData = CaseData.builder()
            .respondentsMigrated(respondentService.setMigratedValue(caseData))
            .respondents1(respondentService.expandRespondentCollection(caseData))
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(mapper.convertValue(alteredData, Map.class))
            .build();
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
    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        CaseData alteredData = respondentService.addHiddenValues(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(mapper.convertValue(alteredData, Map.class))
            .build();
    }

    @SuppressWarnings("unchecked")
    private List<String> validate(CaseDetails caseDetails) {
        ImmutableList.Builder<String> errors = ImmutableList.builder();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        Respondents respondents = defaultIfNull(caseData.getRespondents(), null);

        if (caseData.getRespondents1() != null) {
            List<Element<MigratedRespondent>> migratedRespondentObject = caseData.getRespondents1();

            List<MigratedRespondent> migratedRespondents = migratedRespondentObject.stream()
                .map(Element::getValue)
                .collect(toList());

            if (migratedRespondents.stream()
                .map(MigratedRespondent::getParty)
                .map(Party::getDateOfBirth)
                .filter(Objects::nonNull)
                .anyMatch(dob -> dob.after(new Date()))) {
                errors.add("Date of birth cannot be in the future");
            }
        } else if (respondents != null) {
            if (respondents.getAllRespondents().stream()
                .map(Respondent::getDob)
                .filter(Objects::nonNull)
                .anyMatch(dateOfBirth -> dateOfBirth.after(new Date()))) {
                errors.add("Date of birth cannot be in the future");
            }
        }
        return errors.build();
    }
}
