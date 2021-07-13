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
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.AfterSubmissionCaseDataUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ConfidentialDetailsService;
import uk.gov.hmcts.reform.fpl.service.NoticeOfChangeService;
import uk.gov.hmcts.reform.fpl.service.RespondentAfterSubmissionRepresentationService;
import uk.gov.hmcts.reform.fpl.service.children.ChildRepresentationService;
import uk.gov.hmcts.reform.fpl.service.children.ChildRepresentativeSolicitorValidator;

import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType.CHILD;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NOT_SPECIFIED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
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
    private final NoticeOfChangeService noticeOfChangeService;
    private final RespondentAfterSubmissionRepresentationService respondentAfterSubmissionRepresentationService;

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
        if (!OPEN.equals(caseData.getState())
            && !cafcassSolicitorHasNeverBeenSet(getCaseData(caseDetails), getCaseDataBefore(callbackRequest))) {
            caseDetails.getData().putAll(respondentAfterSubmissionRepresentationService.updateRepresentation(
                getCaseData(caseDetails), getCaseDataBefore(callbackRequest), SolicitorRole.Representing.CHILD,
                isNotFirstTimeRecordingSolicitor(getCaseData(caseDetails), getCaseDataBefore(callbackRequest))
            ));
        }

        confidentialDetailsService.addConfidentialDetailsToCase(
            caseDetails, getCaseData(caseDetails).getAllChildren(), CHILD
        );

        removeTemporaryFields(caseDetails, caseData.getChildrenEventData().getTransientFields());

        return respond(caseDetails);
    }

    private boolean cafcassSolicitorHasNeverBeenSet(CaseData caseData,
                                                    CaseData caseDataBefore) {
        return Set.of(NOT_SPECIFIED, NO)
            .contains(YesNo.fromString(caseDataBefore.getChildrenEventData().getChildrenHaveRepresentation()))
            && YesNo.NO == YesNo.fromString(caseData.getChildrenEventData().getChildrenHaveRepresentation());
    }

    private boolean isNotFirstTimeRecordingSolicitor(CaseData caseData,
                                                     CaseData caseDataBefore) {
        return YES == YesNo.fromString(caseDataBefore.getChildrenEventData().getChildrenHaveRepresentation())
            && YES == YesNo.fromString(caseData.getChildrenEventData().getChildrenHaveRepresentation());
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseData caseDataBefore = getCaseDataBefore(callbackRequest);

        if (!OPEN.equals(caseData.getState())) {
            noticeOfChangeService.updateRepresentativesAccess(
                caseData, caseDataBefore, SolicitorRole.Representing.CHILD
            );
            // TODO: send an email
            publishEvent(new AfterSubmissionCaseDataUpdated(caseData, caseDataBefore));
        }
    }
}
