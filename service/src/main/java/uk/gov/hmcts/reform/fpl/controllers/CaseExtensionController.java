package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ChildExtensionEventData;
import uk.gov.hmcts.reform.fpl.service.CaseExtensionService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.validation.groups.CaseExtensionGroup;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;


@RestController
@RequestMapping("/callback/case-extension")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseExtensionController extends CallbackController {
    private final ValidateGroupService validateGroupService;
    private final CaseExtensionService caseExtensionService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStartEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        caseDetails.getData().putAll(caseExtensionService.prePopulateFields(caseData));
        return respond(caseDetails);
    }

    @PostMapping("/pre-populate/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEventPrePopulation(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<String> errors = caseExtensionService.validateChildSelector(caseData);

        if (errors.isEmpty()) {
            if (YES.getValue().equals(caseData.getChildExtensionEventData().getExtensionForAllChildren())) {
                caseDetails.getData().putAll(caseExtensionService.getAllChildren(caseData));
            } else {
                caseDetails.getData().putAll(caseExtensionService.getSelectedChildren(caseData));
            }
        }
        return respond(caseDetails, errors);
    }


    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        ChildExtensionEventData childExtensionEventData = caseData.getChildExtensionEventData();
        List<String> errors;
        if (YES.getValue().equals(childExtensionEventData.getSameExtensionForAllChildren())) {
            errors = validateGroupService.validateGroup(childExtensionEventData.getChildExtensionAll(),
                    CaseExtensionGroup.class);
        } else {
            errors =  caseExtensionService.validateChildExtensionDate(caseData);
        }
        return respond(caseDetails, errors);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        ChildExtensionEventData childExtensionEventData = caseData.getChildExtensionEventData();

        List<Element<Child>> children1;
        if (YES.getValue().equals(childExtensionEventData.getExtensionForAllChildren())
                && YES.getValue().equals(childExtensionEventData.getSameExtensionForAllChildren())) {
            children1 = caseExtensionService.updateAllChildrenExtension(caseData);
        } else if (NO.getValue().equals(childExtensionEventData.getExtensionForAllChildren())
                && YES.getValue().equals(childExtensionEventData.getSameExtensionForAllChildren())) {
            children1 = caseExtensionService.updateAllSelectedChildrenExtension(caseData);
        } else {
            children1 = caseExtensionService.updateChildrenExtension(caseData);
        }
        caseDetails.getData().put("caseCompletionDate",
                caseExtensionService.getMaxExtendedTimeLine(caseData, children1));

        caseDetails.getData().put("caseSummaryExtensionDetails",
                caseExtensionService.getCaseSummaryExtensionDetails(caseData, children1));

        caseDetails.getData().put("children1", children1);
        removeTemporaryFields(caseDetails, ChildExtensionEventData.class);
        return respond(caseDetails);
    }
}
