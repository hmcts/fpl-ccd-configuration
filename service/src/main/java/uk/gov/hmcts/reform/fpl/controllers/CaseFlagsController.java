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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.caseflag.CaseFlagsService;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

/**
 * REST controller for the CaseFlags event.
 */
@Slf4j
@RequestMapping("/caseFlags")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseFlagsController extends CallbackController {

    private final CaseFlagsService caseFlagsService;

    /**
     * handleAboutToSubmit for creating/managing case flags. Validates flags and sets other flags as needed.
     *
     * @param ccdRequest holds the request and case data
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest ccdRequest) {

        CaseData caseData = getCaseData(ccdRequest);
        caseFlagsService.processNewlySetCaseFlags(caseData);
        ccdRequest.getCaseDetails().getData().put(
            "caseInterpreterRequiredFlag", caseData.getCaseInterpreterRequiredFlag());
        ccdRequest.getCaseDetails().getData().put(
            "caseAdditionalSecurityFlag", caseData.getCaseAdditionalSecurityFlag());

        return respond(ccdRequest.getCaseDetails());
    }
}
