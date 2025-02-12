package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.events.AfterSubmissionCaseDataUpdated;

@RestController
@RequestMapping("/callback/case-summary")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseSummaryController extends CallbackController {

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        publishEvent(
            new AfterSubmissionCaseDataUpdated(getCaseData(callbackRequest), getCaseDataBefore(callbackRequest))
        );
    }
}
