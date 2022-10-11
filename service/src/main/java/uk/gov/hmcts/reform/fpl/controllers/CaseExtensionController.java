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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.CaseExtensionService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.validation.groups.CaseExtensionGroup;
import uk.gov.hmcts.reform.fpl.components.OptionCountBuilder;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.model.order.selector.Selector.newSelector;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;


@Api
@RestController
@RequestMapping("/callback/case-extension")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseExtensionController extends CallbackController {
    private final ValidateGroupService validateGroupService;
    private final CaseExtensionService caseExtensionService;
    private final OptionCountBuilder optionCountBuilder;

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
        caseDetails.getData().putAll(caseExtensionService.getSelectedChildren(caseData));

        return respond(caseDetails, errors);
    }


    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        LocalDate eightWeekExtensionDate = caseExtensionService.getCaseCompletionDateFor8WeekExtension(caseData);

        caseDetails.getData().put("extensionDateEightWeeks", formatLocalDateToString(eightWeekExtensionDate, DATE));

        return respond(caseDetails, validateGroupService.validateGroup(caseData, CaseExtensionGroup.class));
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        LocalDate caseCompletionDate = caseExtensionService.getCaseCompletionDate(caseData);
        caseDetails.getData().put("caseCompletionDate", caseCompletionDate);
        caseDetails.getData().put("children1", caseExtensionService.updateChildrenExtension(caseData));

        return respond(caseDetails);
    }
}
