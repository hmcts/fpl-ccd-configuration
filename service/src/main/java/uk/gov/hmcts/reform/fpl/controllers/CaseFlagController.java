package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.summary.CaseSummaryCaseFlagGenerator;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NOT_SPECIFIED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@RestController
@RequestMapping("/callback/add-case-flag")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseFlagController extends CallbackController {

    private final CaseSummaryCaseFlagGenerator caseSummaryCaseFlagGenerator;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(callbackRequest);
        CaseData caseDataBefore = getCaseDataBefore(callbackRequest);

        YesNo currentCaseFlag = YesNo.fromString(caseData.getCaseFlagAdded());
        YesNo oldCaseFlag = YesNo.fromString(caseDataBefore.getCaseFlagAdded());
        caseData.setCaseFlagValueUpdated(YesNo.from(YES == currentCaseFlag
            && List.of(NOT_SPECIFIED, NO).contains(oldCaseFlag)));

        // generate case flag summary fields in the about-to-submit
        caseDetails.getData().putAll(caseSummaryCaseFlagGenerator.generateFields(caseData));
        return respond(caseDetails);
    }

}
