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
import uk.gov.hmcts.reform.fpl.events.LegalRepresentativesChangeToCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.LegalRepresentativeService;
import uk.gov.hmcts.reform.fpl.service.validators.ManageLegalRepresentativesValidator;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Api
@RestController
@RequestMapping("/callback/manage-legal-representatives")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageLegalRepresentativesController extends CallbackController {
    private final LegalRepresentativeService legaRepresentativeService;
    private final ManageLegalRepresentativesValidator manageLegalRepresentativesValidator;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        return respond(callbackRequest.getCaseDetails());
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        final List<String> validationErrors =
            manageLegalRepresentativesValidator.validate(caseData.getLegalRepresentatives());

        return respond(caseDetails, validationErrors);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails updatedCaseDetails = callbackRequest.getCaseDetails();
        CaseData updatedCaseData = getCaseData(updatedCaseDetails);
        CaseData originalCaseData = getCaseDataBefore(callbackRequest);

        legaRepresentativeService.updateRepresentatives(
            updatedCaseData.getId(),
            unwrapElements(originalCaseData.getLegalRepresentatives()),
            unwrapElements(updatedCaseData.getLegalRepresentatives())
        );
        return respond(updatedCaseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        publishEvent(new LegalRepresentativesChangeToCaseEvent(
            getCaseData(callbackRequest),
            getCaseDataBefore(callbackRequest))
        );
    }

}
