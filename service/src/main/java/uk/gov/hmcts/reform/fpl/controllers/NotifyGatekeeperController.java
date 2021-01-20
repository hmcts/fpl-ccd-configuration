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
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeepersEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionsService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.validation.groups.ValidateFamilyManCaseNumberGroup;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.controllers.ReturnApplicationController.RETURN_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@Api
@RestController
@RequestMapping("/callback/notify-gatekeeper")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotifyGatekeeperController extends CallbackController {
    private static final String GATEKEEPER_EMAIL_KEY = "gatekeeperEmails";
    private final ValidateGroupService validateGroupService;
    private final StandardDirectionsService standardDirectionsService;

    //TODO: can we validate a hearing has been added at this point? Saves some nasty exceptions in the case of
    // no hearing being present when populating standard directions FPLA-1516
    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStartEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<String> errors = new ArrayList<>();
        if (SUBMITTED.equals(caseData.getState())) {
            errors = validateGroupService.validateGroup(caseData, ValidateFamilyManCaseNumberGroup.class);
        }

        caseDetails.getData().put(GATEKEEPER_EMAIL_KEY, resetGateKeeperEmailCollection());
        caseDetails.getData().put(RETURN_APPLICATION, null);

        return respond(caseDetails, errors);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        CaseData caseData = getCaseData(caseDetails);
        CaseData caseDataBefore = getCaseData(caseDetailsBefore);


        if (SUBMITTED.equals(caseDataBefore.getState())) {
            caseDetails.getData().putAll(standardDirectionsService.populateStandardDirections(caseData));
        }

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest);

        publishEvent(new NotifyGatekeepersEvent(caseData));
    }

    private List<Element<EmailAddress>> resetGateKeeperEmailCollection() {
        return wrapElements(EmailAddress.builder().email("").build());
    }
}
