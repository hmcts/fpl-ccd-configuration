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
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.CaseDataChanged;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityUserService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityValidationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/case-initiation")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseInitiationController extends CallbackController {
    private final LocalAuthorityService localAuthorityNameService;
    private final LocalAuthorityUserService localAuthorityUserService;
    private final LocalAuthorityValidationService localAuthorityOnboardedValidationService;

    private final FeatureToggleService featureToggleService;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;

    private final RequestData requestData;

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(
        @RequestBody CallbackRequest callbackrequest) {
        String caseLocalAuthority = localAuthorityNameService.getLocalAuthorityCode();
        String localAuthorityName = localAuthorityNameLookupConfiguration.getLocalAuthorityName(caseLocalAuthority);
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        String currentUser = requestData.userId();

        List<String> errors = new ArrayList<>();

        if (featureToggleService.isBlockCasesForLocalAuthoritiesNotOnboardedEnabled(localAuthorityName)) {
            errors = localAuthorityOnboardedValidationService.validateIfLaIsOnboarded(caseLocalAuthority, currentUser);
        }

        return respond(caseDetails, errors);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitEvent(
        @RequestBody CallbackRequest callbackrequest) {
        String caseLocalAuthority = localAuthorityNameService.getLocalAuthorityCode();
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        Map<String, Object> data = caseDetails.getData();
        data.put("caseLocalAuthority", caseLocalAuthority);

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
