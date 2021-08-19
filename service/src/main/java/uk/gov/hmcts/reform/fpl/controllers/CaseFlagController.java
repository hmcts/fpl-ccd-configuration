package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.events.AfterSubmissionCaseDataUpdated;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;

@Api
@RestController
@RequestMapping("/callback/add-case-flag")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseFlagController extends CallbackController {


    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {

        publishEvent(new AfterSubmissionCaseDataUpdated(getCaseData(callbackRequest),
            getCaseDataBefore(callbackRequest)));
    }
}
