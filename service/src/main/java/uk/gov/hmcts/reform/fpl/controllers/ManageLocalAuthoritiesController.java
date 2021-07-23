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
import uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.LocalAuthoritiesEventData;
import uk.gov.hmcts.reform.fpl.service.CaseAssignmentService;
import uk.gov.hmcts.reform.fpl.service.ManageLocalAuthoritiesService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.ADD;
import static uk.gov.hmcts.reform.fpl.enums.LocalAuthorityAction.REMOVE;

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

        return respond(caseDetails);
    }

    @PostMapping("local-authority/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleActionSelection(@RequestBody CallbackRequest request) {

        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);
        final LocalAuthoritiesEventData eventData = caseData.getLocalAuthoritiesEventData();
        final LocalAuthorityAction action = eventData.getLocalAuthorityAction();

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

        return respond(caseDetails);
    }

    @PostMapping("local-authority-validate/mid-event")
    public AboutToStartOrSubmitCallbackResponse validate(@RequestBody CallbackRequest request) {

        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);

        final List<String> errors = service.validateLocalAuthorityEmail(caseData.getLocalAuthoritiesEventData());

        return respond(caseDetails, errors);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {

        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);

        final LocalAuthorityAction action = caseData.getLocalAuthoritiesEventData().getLocalAuthorityAction();

        if (ADD.equals(action)) {
            caseDetails.getData().put("sharedLocalAuthorityPolicy", service.getSharedLocalAuthorityPolicy(caseData));
            caseDetails.getData().put("localAuthorities", service.addSharedLocalAuthority(caseData));

            return respond(removeTemporaryFields(caseDetails));
        }

        if (REMOVE.equals(action)) {
            caseDetails.getData().put("changeOrganisationRequestField", service.getOrgRemovalRequest(caseData));
            caseDetails.getData().put("localAuthorities", service.removeSharedLocalAuthority(caseData));

            return assignmentService.applyDecisionAsSystemUser(removeTemporaryFields(caseDetails));
        }

        return respond(removeTemporaryFields(caseDetails));
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest request) {

        final CaseData caseDataBefore = getCaseDataBefore(request);
        final CaseData caseData = getCaseData(request);

        service.getChangeEvent(caseData, caseDataBefore).ifPresent(this::publishEvent);
    }

    private static CaseDetails removeTemporaryFields(CaseDetails caseDetails) {

        return CaseDetailsHelper.removeTemporaryFields(caseDetails, LocalAuthoritiesEventData.class);
    }

}
