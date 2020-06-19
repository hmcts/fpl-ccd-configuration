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
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityUserService;

import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/case-initiation")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseInitiationController {
    private final LocalAuthorityService localAuthorityNameService;
    private final LocalAuthorityUserService localAuthorityUserService;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitEvent(
        @RequestBody CallbackRequest callbackrequest) {
        String caseLocalAuthority = localAuthorityNameService.getLocalAuthorityCode();
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        Map<String, Object> data = caseDetails.getData();
        data.put("caseLocalAuthority", caseLocalAuthority);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = Long.toString(caseDetails.getId());
        String caseLocalAuthority = (String) caseDetails.getData()
            .get("caseLocalAuthority");

        localAuthorityUserService.grantUserAccessWithCaseRole(caseId, caseLocalAuthority);
    }
}
