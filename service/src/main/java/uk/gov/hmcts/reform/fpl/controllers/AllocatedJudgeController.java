package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.AfterSubmissionCaseDataUpdated;
import uk.gov.hmcts.reform.fpl.events.judicial.NewAllocatedJudgeEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.event.AllocateJudgeEventData;
import uk.gov.hmcts.reform.fpl.service.JudicialService;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;

@Slf4j
@RestController
@RequestMapping("/callback/allocated-judge")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AllocatedJudgeController extends CallbackController {
    private final ValidateEmailService validateEmailService;
    private final JudicialService judicialService;

    private static final String ALLOCATED_JUDGE = "allocatedJudge";

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData()
            .putAll(judicialService.populateEventDataMapFromJudge(caseData.getAllocatedJudge()));
        return respond(caseDetails);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        AllocateJudgeEventData eventData = caseData.getAllocateJudgeEventData();

        Optional<String> error = judicialService.validateAllocatedJudge(eventData);

        if (error.isPresent()) {
            return respond(caseDetails, List.of(error.get()));
        }

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        AllocateJudgeEventData eventData = caseData.getAllocateJudgeEventData();

        Optional<String> error = judicialService.validateAllocatedJudge(eventData);

        if (error.isPresent()) {
            return respond(caseDetails, List.of(error.get()));
        }

        caseDetails.getData().put(ALLOCATED_JUDGE, judicialService.buildAllocatedJudgeFromEventData(eventData));

        removeTemporaryFields(caseDetails, AllocateJudgeEventData.class);
        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        publishEvent(new NewAllocatedJudgeEvent(caseData.getAllocatedJudge(), caseData.getId()));
        publishEvent(
            new AfterSubmissionCaseDataUpdated(getCaseData(callbackRequest), getCaseDataBefore(callbackRequest))
        );

    }
}
