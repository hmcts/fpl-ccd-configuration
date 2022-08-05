package uk.gov.hmcts.reform.fpl.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApiV2;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.CaseLocation;
import uk.gov.hmcts.reform.fpl.events.CaseDataChanged;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CaseInitiationService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;

@Api
@RestController
@RequestMapping("/callback/case-initiation")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseInitiationController extends CallbackController {

    private final CaseInitiationService caseInitiationService;

    private final CoreCaseDataApiV2 coreCaseDataApi;

    private final RequestData requestData;

    private final AuthTokenGenerator authToken;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        final CaseDetailsMap caseData = caseDetailsMap(callbackrequest.getCaseDetails());

        caseInitiationService.getUserOrganisationId().ifPresent(organisationId ->
            caseInitiationService.getOutsourcingType(organisationId).ifPresent(outsourcingType -> {
                caseData.putIfNotEmpty("outsourcingType", outsourcingType);
                caseData.putIfNotEmpty("outsourcingLAs", caseInitiationService
                    .getOutsourcingLocalAuthorities(organisationId, outsourcingType));
            }));

        return respond(caseData);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        final CaseDetails caseDetails = callbackrequest.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);

        return respond(caseDetails, caseInitiationService.checkUserAllowedToCreateCase(caseData));
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackrequest) {
        final CaseData caseData = getCaseData(callbackrequest);
        final CaseDetailsMap caseDetails = caseDetailsMap(callbackrequest.getCaseDetails());

        final CaseData updatedCaseData = caseInitiationService.updateOrganisationsDetails(caseData);

        caseDetails.putIfNotEmpty("caseLocalAuthority", updatedCaseData.getCaseLocalAuthority());
        caseDetails.putIfNotEmpty("caseLocalAuthorityName", updatedCaseData.getCaseLocalAuthorityName());
        caseDetails.putIfNotEmpty("localAuthorityPolicy", updatedCaseData.getLocalAuthorityPolicy());
        caseDetails.putIfNotEmpty("outsourcingPolicy", updatedCaseData.getOutsourcingPolicy());
        caseDetails.putIfNotEmpty("court", updatedCaseData.getCourt());
        caseDetails.putIfNotEmpty("multiCourts", updatedCaseData.getMultiCourts());
        caseDetails.putIfNotEmpty("caseNameHmctsRestricted", updatedCaseData.getCaseName());
        caseDetails.putIfNotEmpty("caseNameHmctsInternal", updatedCaseData.getCaseName());
        caseDetails.putIfNotEmpty("caseNamePublic", updatedCaseData.getCaseName());
        // TODO filling in caseManagementLocation with anything for testing
        caseDetails.putIfNotEmpty("caseManagementLocation", CaseLocation.builder()
            .baseLocation("1")
            .region("3").build());
        // TODO filling in caseManagementCategory
        caseDetails.putIfNotEmpty("caseManagementCategory", DynamicList.builder()
            .value(DynamicListElement.builder().code("987").label("Category Label").build())
            .listItems(List.of(
                DynamicListElement.builder().code("987").label("Category Label").build()
            ))
            .build());

        caseDetails.removeAll("outsourcingType", "outsourcingLAs");

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        final CaseData caseData = getCaseData(callbackRequest);

        caseInitiationService.grantCaseAccess(caseData);

        publishEvent(new CaseDataChanged(caseData));

        // update supplementary data
        String caseId = caseData.getId().toString();
        Map<String, Map<String, Map<String, Object>>> supplementaryData = new HashMap<>();
        supplementaryData.put("supplementary_data_updates",
            Map.of("$set", Map.of("HMCTSServiceId", "ABA3")));
        coreCaseDataApi.submitSupplementaryData(requestData.authorisation(), authToken.generate(), caseId,
            supplementaryData);
    }
}
