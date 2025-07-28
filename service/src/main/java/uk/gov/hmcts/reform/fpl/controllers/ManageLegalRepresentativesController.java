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
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.events.LegalRepresentativesUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.LegalRepresentativeService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;
import uk.gov.hmcts.reform.fpl.service.validators.ManageLegalRepresentativesValidator;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@RestController
@RequestMapping("/callback/manage-legal-representatives")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageLegalRepresentativesController extends CallbackController {
    private final LegalRepresentativeService legalRepresentativeService;
    private final ManageLegalRepresentativesValidator manageLegalRepresentativesValidator;
    private final ValidateEmailService validateEmailService;
    private final UserService userService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData()
            .put("legalRepresentatives", legalRepresentativeService.getDefaultLegalRepresentatives(caseData));

        return respond(callbackRequest.getCaseDetails());
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<String> emails = caseData.getLegalRepresentatives().stream()
            .map(Element::getValue)
            .map(LegalRepresentative::getEmail)
            .collect(Collectors.toList());

        List<String> errors = validateEmailService.validate(emails, "LA Legal Representative");

        final List<String> emailNotRegisteredErrors =
            manageLegalRepresentativesValidator.validate(caseData.getLegalRepresentatives());

        errors.addAll(emailNotRegisteredErrors);

        if (userService.getCaseRoles(caseDetails.getId()).contains(CaseRole.LABARRISTER)) {
            String userEmail = userService.getUserEmail();
            CaseData caseDataBefore = getCaseData(callbackRequest.getCaseDetailsBefore());
            boolean laBarristerRemovedOtherEntry = caseDataBefore.getLegalRepresentatives().stream()
                .anyMatch(legalRepBefore ->
                    findElement(legalRepBefore.getId(), caseData.getLegalRepresentatives()).isEmpty()
                        && !Objects.equals(legalRepBefore.getValue().getEmail(), userEmail));
            if (laBarristerRemovedOtherEntry) {
                errors.add("You cannot remove another legal representative from the case, "
                    + "you can only remove yourself.");
            }
        }

        if (!errors.isEmpty()) {
            return respond(caseDetails, errors);
        }

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails updatedCaseDetails = callbackRequest.getCaseDetails();
        CaseData updatedCaseData = getCaseData(updatedCaseDetails);
        CaseData originalCaseData = getCaseDataBefore(callbackRequest);

        legalRepresentativeService.updateRepresentatives(
            updatedCaseData.getId(),
            unwrapElements(originalCaseData.getLegalRepresentatives()),
            unwrapElements(updatedCaseData.getLegalRepresentatives())
        );
        return respond(updatedCaseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        publishEvent(new LegalRepresentativesUpdated(
            getCaseData(callbackRequest),
            getCaseDataBefore(callbackRequest))
        );
    }

}
