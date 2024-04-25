package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ChangeCaseStateService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.validation.groups.MigrateStateGroup;

import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;

@RestController
@RequestMapping("/callback/change-state")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChangeStateController extends CallbackController {
    private final ChangeCaseStateService changeCaseStateService;
    private final ValidateGroupService validateGroupService;

    @PostMapping("/about-to-start")
    public CallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().putAll(changeCaseStateService.initialiseEventFields(caseData));

        return respond(caseDetails);
    }

    @PostMapping("/mid-event")
    public CallbackResponse handleMidEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        return respond(caseDetails, validateGroupService.validateGroup(caseData, MigrateStateGroup.class));
    }

    @PostMapping("/about-to-submit")
    public CallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().putAll(changeCaseStateService.updateCaseState(caseData));
        removeTemporaryFields(caseDetails, "confirmChangeState", "nextStateLabelContent", "closedStateRadioList");

        return respond(caseDetails);
    }
}
