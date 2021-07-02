package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ConfidentialDetailsService;
import uk.gov.hmcts.reform.fpl.service.children.ChildRepresentationService;
import uk.gov.hmcts.reform.fpl.service.children.ChildRepresentativeSolicitorValidator;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType.CHILD;
import static uk.gov.hmcts.reform.fpl.model.Child.expandCollection;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;

@Api
@RestController
@RequestMapping("/callback/enter-children")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ChildController extends CallbackController {
    private final ConfidentialDetailsService confidentialDetailsService;
    private final ChildRepresentationService childRepresentationService;
    private final ChildRepresentativeSolicitorValidator validator;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().put("children1", confidentialDetailsService.prepareCollection(
            caseData.getAllChildren(), caseData.getConfidentialChildren(), expandCollection()
        ));

        return respond(caseDetails);
    }

    @PostMapping("/representation-details/mid-event")
    public CallbackResponse handleRepresentationDetailsMidEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<String> errors = validator.validateMainChildRepresentative(caseData);

        if (!errors.isEmpty()) {
            return respond(caseDetails, errors);
        }

        caseDetails.getData().putAll(childRepresentationService.populateRepresentationDetails(caseData));

        return respond(caseDetails);
    }

    @PostMapping("/representation-validation/mid-event")
    public CallbackResponse handleRepresentationValidationMidEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();

        List<String> errors = validator.validateChildRepresentationDetails(getCaseData(caseDetails));

        return respond(caseDetails, errors);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().putAll(childRepresentationService.finaliseRepresentationDetails(caseData));
        caseDetails.getData().putAll(childRepresentationService.generateCaseAccessFields(getCaseData(caseDetails)));

        confidentialDetailsService.addConfidentialDetailsToCase(
            caseDetails, getCaseData(caseDetails).getAllChildren(), CHILD
        );

        removeTemporaryFields(caseDetails, caseData.getChildrenEventData().getTransientFields());

        return respond(caseDetails);
    }
}
