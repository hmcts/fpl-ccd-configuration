package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.AfterSubmissionCaseDataUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NOT_SPECIFIED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Api
@RestController
@RequestMapping("/callback/add-case-flag")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseFlagController extends CallbackController {

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest);
        CaseData caseDataBefore = getCaseDataBefore(callbackRequest);

        YesNo currentCaseFlag = YesNo.fromString(caseData.getCaseFlagAdded());
        YesNo oldCaseFlag = YesNo.fromString(caseDataBefore.getCaseFlagAdded());
        caseData.setCaseFlagValueUpdated(YesNo.from(YES == currentCaseFlag
            && List.of(NOT_SPECIFIED, NO).contains(oldCaseFlag)));

        publishEvent(new AfterSubmissionCaseDataUpdated(caseData, caseDataBefore));
    }
}
