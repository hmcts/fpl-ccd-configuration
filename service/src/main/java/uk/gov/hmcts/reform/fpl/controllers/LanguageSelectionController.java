package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.service.translation.C110ATranslationRequirementCalculator;

@Slf4j
@RestController
@RequestMapping("/callback/language-selection")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LanguageSelectionController extends CallbackController {

    private final C110ATranslationRequirementCalculator calculator;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        caseDetails.getData()
            .put("submittedFormTranslationRequirements", calculator.calculate(getCaseData(caseDetails)));
        caseDetails.getData()
            .put("submittedFormNeedTranslation", getCaseData(caseDetails).getC110A().getSubmittedFormNeedTranslation());
        return respond(caseDetails);
    }
}
