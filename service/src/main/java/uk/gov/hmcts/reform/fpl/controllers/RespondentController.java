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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.service.RespondentService;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

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

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        caseDetails.getData().put("respondents1", respondentService.expandRespondentCollection(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
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

    //TODO RespondentControllerAboutToSubmitTest (or combine the 3 controller test files into one for bonus points...)
    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<Respondent>> confidentialRespondents = respondentService.buildConfidentialRespondentsList(caseData);
        if (confidentialRespondents.size() != 0) {
            caseDetails.getData().put("confidentialRespondents", confidentialRespondents);
        } else {
            caseDetails.getData().remove("confidentialRespondents");
        }

        //Fixes expand collection 'bug' if user removes all respondents and submits (will not re-open collection)
        if (respondentService.expandedCollectionNotEmpty(caseData.getRespondents1())) {
            caseDetails.getData().put("respondents1", respondentService.modifyHiddenValues(caseData));
        } else {
            caseDetails.getData().remove("respondents1");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @SuppressWarnings("unchecked")
    private List<String> validate(CaseDetails caseDetails) {
        ImmutableList.Builder<String> errors = ImmutableList.builder();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (caseData.getRespondents1().stream()
            .map(Element::getValue)
            .map(Respondent::getParty)
            .map(Party::getDateOfBirth)
            .filter(Objects::nonNull)
            .anyMatch(dob -> dob.isAfter(LocalDate.now()))) {
            errors.add("Date of birth cannot be in the future");
        }

        return errors.build();
    }
}
