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
import uk.gov.hmcts.reform.fpl.events.CaseDataChanged;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityUserService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityValidationService;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;

import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/case-initiation")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseInitiationController extends CallbackController {
    private final LocalAuthorityService localAuthorityNameService;
    private final LocalAuthorityUserService localAuthorityUserService;
    private final LocalAuthorityValidationService localAuthorityOnboardedValidationService;
    private final OrganisationService organisationService;

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(
        @RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        return respond(caseDetails, localAuthorityOnboardedValidationService.validateIfUserIsOnboarded());
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitEvent(
        @RequestBody CallbackRequest callbackrequest) {
        String caseLocalAuthority = localAuthorityNameService.getLocalAuthorityCode();
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        Map<String, Object> data = caseDetails.getData();
        data.put("caseLocalAuthority", caseLocalAuthority);
        organisationService.findOrganisationPolicy().ifPresent(policy -> data.put("localAuthorityPolicy", policy));

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest);

        localAuthorityUserService.grantUserAccessWithCaseRole(caseData.getId().toString(),
            caseData.getCaseLocalAuthority());
        publishEvent(new CaseDataChanged(caseData));
    }
}
