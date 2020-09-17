package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.CaseDataChanged;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityUserService;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;

import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/case-initiation")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseInitiationController extends CallbackController {
    private final LocalAuthorityService localAuthorityNameService;
    private final LocalAuthorityUserService localAuthorityUserService;

    private final FeatureToggleService featureToggleService;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final OrganisationService organisationService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStartEvent(
        @RequestBody CallbackRequest callbackrequest) {
        String caseLocalAuthority = localAuthorityNameService.getLocalAuthorityCode();
        String localAuthorityName = localAuthorityNameLookupConfiguration.getLocalAuthorityName(caseLocalAuthority);
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        Map<String, Object> data = caseDetails.getData();

        if (featureToggleService.isMigrateToManageOrgWarningPageEnabled(localAuthorityName)) {
            data.put("pageShow", "YES");
        }

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitEvent(
        @RequestBody CallbackRequest callbackrequest) {
        String caseLocalAuthority = localAuthorityNameService.getLocalAuthorityCode();
        CaseDetails caseDetails = callbackrequest.getCaseDetails();


        Map<String, Object> data = caseDetails.getData();
        data.put("caseLocalAuthority", caseLocalAuthority);

        data.remove("pageShow");

        organisationService.findOrganisation()
            .map(org -> OrganisationPolicy.builder()
                .organisation(uk.gov.hmcts.reform.ccd.Organisation.builder()
                    .organisationName(org.getName())
                    .organisationID(org.getName())
                    .build())
                .orgPolicyCaseAssignedRole("[LASOLICITOR]")
                .orgPolicyReference("test")
                .build())
            .ifPresent(policy -> data.put("localAuthorityPolicy", policy));

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
