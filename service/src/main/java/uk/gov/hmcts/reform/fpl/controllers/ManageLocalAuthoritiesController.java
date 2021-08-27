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
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.LocalAuthoritiesEventData;
import uk.gov.hmcts.reform.fpl.service.CaseAssignmentService;
import uk.gov.hmcts.reform.fpl.service.ManageLocalAuthoritiesService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.ADD;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.REMOVE;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.TRANSFER;

@Api
@RestController
@RequestMapping("/callback/manage-local-authorities")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageLocalAuthoritiesController extends CallbackController {

    private final ManageLocalAuthoritiesService service;
    private final CaseAssignmentService assignmentService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {

        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().put("localAuthoritiesToShare", service.getLocalAuthoritiesToShare(caseData));
        caseDetails.getData().put("sharedLocalAuthority", service.getSharedLocalAuthorityName(caseData));

        return respond(caseDetails);
    }

    @PostMapping("action-selection/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleActionSelection(@RequestBody CallbackRequest request) {

        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);
        final LocalAuthoritiesEventData eventData = caseData.getLocalAuthoritiesEventData();
        final LocalAuthorityAction action = getAction(caseData);

        final List<String> errors = service.validateAction(caseData);

        if (isNotEmpty(errors)) {
            return respond(caseDetails, errors);
        }

        if (ADD == action) {
            caseDetails.getData().put("localAuthorityEmail", service.getSelectedLocalAuthorityEmail(eventData));
        }

        if (REMOVE == action) {
            caseDetails.getData().put("localAuthorityToRemove", service.getSharedLocalAuthorityName(caseData));
        }

        if (TRANSFER == action) {
            final DynamicList localAuthoritiesToTransfer = service.getLocalAuthoritiesToTransfer(caseData);
            caseDetails.getData().put("localAuthoritiesToTransfer", localAuthoritiesToTransfer);
            caseDetails.getData().put("localAuthoritiesToTransferWithoutShared", localAuthoritiesToTransfer);
        }

        return respond(caseDetails);
    }

    @PostMapping("add/la-details/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleLocalAuthorityToAddDetails(@RequestBody CallbackRequest request) {

        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);

        final List<String> errors = service.validateLocalAuthorityToShare(caseData.getLocalAuthoritiesEventData());

        return respond(caseDetails, errors);
    }

    @PostMapping("transfer/la-selection/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleLocalAuthorityToTransferSelection(
        @RequestBody CallbackRequest request) {

        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);

        final LocalAuthority localAuthority = service.getLocalAuthorityToTransferDetails(caseData);

        caseDetails.getData().put("localAuthorityToTransfer", localAuthority);
        caseDetails.getData().put("localAuthorityToTransferSolicitor", localAuthority.getFirstSolicitor());

        return respond(caseDetails);
    }

    @PostMapping("transfer/la-details/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleLocalAuthorityToTransferDetails(
        @RequestBody CallbackRequest request) {

        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);

        final List<String> errors = service.validateLocalAuthorityToTransfer(caseData.getLocalAuthoritiesEventData());

        if (isNotEmpty(errors)) {
            return respond(caseDetails, errors);
        }

        caseDetails.getData().put("currentCourtName", service.getCurrentCourtName(caseData));
        caseDetails.getData().put("courtsToTransfer", service.getCourtsToTransfer(caseData));

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {

        CaseDetails caseDetails = request.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);

        final LocalAuthorityAction action = getAction(caseData);

        if (ADD == action) {
            caseDetails.getData().put("sharedLocalAuthorityPolicy", service.getSharedLocalAuthorityPolicy(caseData));
            caseDetails.getData().put("localAuthorities", service.addSharedLocalAuthority(caseData));

            return respond(removeTemporaryFields(caseDetails));
        }

        if (REMOVE == action) {
            caseDetails.getData().put("changeOrganisationRequestField", service.getOrgRemovalRequest(caseData));
            caseDetails.getData().put("localAuthorities", service.removeSharedLocalAuthority(caseData));

            return assignmentService.applyDecisionAsSystemUser(removeTemporaryFields(caseDetails));
        }

        if (TRANSFER == action) {

            final Organisation newDesignatedOrg = service.transfer(caseData);

            caseDetails.getData().put("court", caseData.getCourt());
            caseDetails.getData().put("caseLocalAuthority", caseData.getCaseLocalAuthority());
            caseDetails.getData().put("caseLocalAuthorityName", caseData.getCaseLocalAuthorityName());
            caseDetails.getData().put("localAuthorities", caseData.getLocalAuthorities());

            removeTemporaryFields(caseDetails);

            final AboutToStartOrSubmitCallbackResponse updateDesignatedPolicy = assignmentService.replaceAsSystemUser(
                caseDetails,
                LASOLICITOR,
                newDesignatedOrg,
                caseData.getLocalAuthorityPolicy().getOrganisation());

            caseDetails = caseDetails.toBuilder()
                .data(updateDesignatedPolicy.getData())
                .build();

            if (service.isSecondary(caseData, newDesignatedOrg)) {
                caseDetails.getData().remove("sharedLocalAuthorityPolicy");
                //CCD-1753 once fixed remove statement above and uncomment one below
                //return assignmentService.removeAsSystemUser(caseDetails, LASHARED, newDesignatedOrg);
            }
        }

        return respond(removeTemporaryFields(caseDetails));
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest request) {

        final CaseData caseDataBefore = getCaseDataBefore(request);
        final CaseData caseData = getCaseData(request);

        service.getChangeEvent(caseData, caseDataBefore).forEach(this::publishEvent);
    }

    private static LocalAuthorityAction getAction(CaseData caseData) {

        return caseData.getLocalAuthoritiesEventData().getLocalAuthorityAction();
    }

    private static CaseDetails removeTemporaryFields(CaseDetails caseDetails) {

        return CaseDetailsHelper.removeTemporaryFields(caseDetails, LocalAuthoritiesEventData.class);
    }

}
