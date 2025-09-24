package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

/**
 * A dummy controller doing nothing. Use this controlelr only when no case data changes but have to trigger
 * interceptors or controller advice.
 */
@RestController
@RequestMapping("/callback/generic-update")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GenericCaseUpdateController extends CallbackController {

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        return respond(request.getCaseDetails());
    }
}
