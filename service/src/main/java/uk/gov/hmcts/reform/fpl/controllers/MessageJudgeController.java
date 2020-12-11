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
import uk.gov.hmcts.reform.fpl.events.NewJudicialMessageEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.MessageJudgeService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData.transientFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;

@Api
@RestController
@RequestMapping("/callback/message-judge")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MessageJudgeController extends CallbackController {
    private final MessageJudgeService messageJudgeService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);

        caseDetailsMap.putAll(messageJudgeService.initialiseCaseFields(caseData));

        return respond(caseDetailsMap);
    }

    @PostMapping("/populate-new-message/mid-event")
    public AboutToStartOrSubmitCallbackResponse handlePopulateNewMessageMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);

        caseData.getFirstHearing().ifPresent(hearingBooking -> caseDetailsMap.put("nextHearingLabel",
            String.format("Next hearing in the case: %s", hearingBooking.toLabel())));

        caseDetailsMap.putAll(messageJudgeService.populateNewMessageFields(caseData));

        return respond(caseDetailsMap);
    }

    @PostMapping("/populate-reply-message/mid-event")
    public AboutToStartOrSubmitCallbackResponse handlePopulateReplyMessageMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);

        caseData.getFirstHearing().ifPresent(hearingBooking -> caseDetailsMap.put("nextHearingLabel",
            String.format("Next hearing in the case: %s", hearingBooking.toLabel())));

        caseDetailsMap.putAll(messageJudgeService.populateReplyMessageFields(caseData));

        return respond(caseDetailsMap);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);

        List<Element<JudicialMessage>> updatedMessages = messageJudgeService.addNewJudicialMessage(caseData);
        caseDetailsMap.put("judicialMessages", messageJudgeService.sortJudicialMessages(updatedMessages));

        removeTemporaryFields(caseDetailsMap, transientFields());

        return respond(caseDetailsMap);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        JudicialMessage newJudicialMessage = caseData.getJudicialMessages().get(0).getValue();

        publishEvent(new NewJudicialMessageEvent(caseData, newJudicialMessage));
    }
}
