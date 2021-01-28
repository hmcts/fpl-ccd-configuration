package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;

import java.util.List;

@Api
@RestController
@RequestMapping("/callback/allocated-judge")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AllocatedJudgeController extends CallbackController {
    private final ValidateEmailService validateEmailService;

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        String email = caseData.getAllocatedJudge().getJudgeEmailAddress();

        String error = validateEmailService.validate(email);

        if (!error.isBlank()) {
            return respond(caseDetails, List.of(error));
        }

        return respond(caseDetails);
    }
}
