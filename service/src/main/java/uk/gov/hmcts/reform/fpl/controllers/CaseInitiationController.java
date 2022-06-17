package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
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
import uk.gov.hmcts.reform.fpl.service.CaseInitiationService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.Objects;

import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;

@Api
@RestController
@RequestMapping("/callback/case-initiation")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseInitiationController extends CallbackController {

    private final CaseInitiationService caseInitiationService;

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
        final String orderListToShow = "orderListToShow";
        final CaseData caseData = getCaseData(callbackrequest);
        final CaseDetailsMap caseDetails = caseDetailsMap(callbackrequest.getCaseDetails());

        final CaseData updatedCaseData = caseInitiationService.updateOrganisationsDetails(caseData);

        caseDetails.putIfNotEmpty("caseLocalAuthority", updatedCaseData.getCaseLocalAuthority());
        caseDetails.putIfNotEmpty("caseLocalAuthorityName", updatedCaseData.getCaseLocalAuthorityName());
        caseDetails.putIfNotEmpty("localAuthorityPolicy", updatedCaseData.getLocalAuthorityPolicy());
        caseDetails.putIfNotEmpty("outsourcingPolicy", updatedCaseData.getOutsourcingPolicy());
        caseDetails.putIfNotEmpty("court", updatedCaseData.getCourt());
        caseDetails.putIfNotEmpty("multiCourts", updatedCaseData.getMultiCourts());

        caseDetails.removeAll("outsourcingType", "outsourcingLAs");

        if (Objects.nonNull(caseData.getRepresentativeType())) {
            if (Objects.equals(caseData.getRepresentativeType().toString(), "LOCAL_AUTHORITY")) {
                caseDetails.remove(orderListToShow);
                caseDetails.putIfNotEmpty(orderListToShow, ImmutableList.of("SHOW_LA"));

            } else if (Objects.equals(caseData.getRepresentativeType().toString(), "RESPONDENT_SOLICITOR") ||
                Objects.equals(caseData.getRepresentativeType().toString(), "CHILD_SOLICITOR")) {
                caseDetails.remove(orderListToShow);
                caseDetails.putIfNotEmpty(orderListToShow, ImmutableList.of("SHOW_SOLICITOR"));
            }
        }

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        final CaseData caseData = getCaseData(callbackRequest);

        caseInitiationService.grantCaseAccess(caseData);

        publishEvent(new CaseDataChanged(caseData));
    }
}
